"""
Caregiver/family access to diabetes profiles.
GET /api/diabetes/profiles/{profile_id}/*

Access rules:
- The profile owner (user_id) can always access their own data.
- A linked caregiver (caregiver_user_id) can read-only access after consent.
- Anyone else gets 403.
"""
from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session
from typing import List
from datetime import datetime, timezone, timedelta

from app.core.database import get_db
from app.core.security import get_current_user
from app.models.user import User
from app.models.diabetes_profile import DiabetesProfile
from app.models.diabetes_record import DiabetesRecord
from app.models.diabetes_medication import DiabetesMedication, DiabetesMedicationLog
from app.models.diabetes_ai_assessment import DiabetesAiAssessment
from app.models.diabetes_alert import DiabetesAlert
from app.schemas.diabetes import (
    DiabetesProfileResponse,
    DiabetesRecordResponse,
    DiabetesMedicationResponse,
    DiabetesAdherenceResponse,
    DiabetesAiAssessmentResponse,
    DiabetesAlertResponse,
    GlucoseTrendsResponse,
    DiabetesDoctorSummaryResponse,
    GlucoseSummary,
    MedicationAdherenceSummary,
    WeeklySummary,
)
from app.routers.diabetes_self import (
    _calc_adherence, _streak_days, _parse_ai_assessment, determine_trend_direction,
    DISCLAIMER_FULL,
)

router = APIRouter(prefix="/api/diabetes/profiles", tags=["Diabetes Caregiver"])


def _verify_access(profile_id: str, current_user: User, db: Session) -> DiabetesProfile:
    profile = db.query(DiabetesProfile).filter(DiabetesProfile.id == profile_id).first()
    if not profile:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Profil topilmadi")

    is_owner = profile.user_id == current_user.id
    is_caregiver = (
        profile.caregiver_user_id == current_user.id and profile.consent_confirmed
    )
    if not (is_owner or is_caregiver):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Bu profilga kirish huquqingiz yo'q",
        )
    return profile


@router.get("/{profile_id}/dashboard")
def get_caregiver_dashboard(
    profile_id: str,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = _verify_access(profile_id, current_user, db)
    week_ago = datetime.now(timezone.utc) - timedelta(days=7)

    latest_glucose = (
        db.query(DiabetesRecord)
        .filter(DiabetesRecord.profile_id == profile.id)
        .order_by(DiabetesRecord.measured_at.desc())
        .first()
    )

    week_records = (
        db.query(DiabetesRecord)
        .filter(
            DiabetesRecord.profile_id == profile.id,
            DiabetesRecord.measured_at >= week_ago,
        )
        .order_by(DiabetesRecord.measured_at.desc())
        .all()
    )

    total, taken, missed, skipped = _calc_adherence(db, profile.id, 7)
    adherence_pct = round((taken / total * 100) if total > 0 else 0.0, 1)

    latest_ai = (
        db.query(DiabetesAiAssessment)
        .filter(DiabetesAiAssessment.profile_id == profile.id)
        .order_by(DiabetesAiAssessment.created_at.desc())
        .first()
    )

    unread = (
        db.query(DiabetesAlert)
        .filter(DiabetesAlert.profile_id == profile.id, DiabetesAlert.is_read == False)
        .count()
    )

    vals = [float(r.glucose_value) for r in week_records]
    weekly = None
    if vals:
        weekly = WeeklySummary(
            average_glucose=round(sum(vals) / len(vals), 2),
            highest_glucose=max(vals),
            lowest_glucose=min(vals),
            trend_direction=determine_trend_direction(week_records),
            total_records=len(week_records),
        )

    return {
        "profile": DiabetesProfileResponse.model_validate(profile),
        "latest_glucose": DiabetesRecordResponse.model_validate(latest_glucose) if latest_glucose else None,
        "medication_adherence": MedicationAdherenceSummary(
            total_scheduled=total,
            total_taken=taken,
            adherence_percent=adherence_pct,
        ),
        "latest_risk_level": latest_ai.risk_level if latest_ai else None,
        "latest_ai_summary": latest_ai.summary if latest_ai else None,
        "unread_alerts": unread,
        "weekly_summary": weekly,
    }


@router.get("/{profile_id}/glucose", response_model=List[DiabetesRecordResponse])
def caregiver_glucose_list(
    profile_id: str,
    period: str = Query("7d"),
    limit: int = Query(30, le=100),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = _verify_access(profile_id, current_user, db)
    days = {"7d": 7, "30d": 30, "90d": 90}.get(period, 7)
    since = datetime.now(timezone.utc) - timedelta(days=days)
    return (
        db.query(DiabetesRecord)
        .filter(
            DiabetesRecord.profile_id == profile.id,
            DiabetesRecord.measured_at >= since,
        )
        .order_by(DiabetesRecord.measured_at.desc())
        .limit(limit)
        .all()
    )


@router.get("/{profile_id}/alerts", response_model=List[DiabetesAlertResponse])
def caregiver_alerts(
    profile_id: str,
    limit: int = Query(20, le=100),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = _verify_access(profile_id, current_user, db)
    return (
        db.query(DiabetesAlert)
        .filter(DiabetesAlert.profile_id == profile.id)
        .order_by(DiabetesAlert.created_at.desc())
        .limit(limit)
        .all()
    )


@router.get("/{profile_id}/doctor-summary", response_model=DiabetesDoctorSummaryResponse)
def caregiver_doctor_summary(
    profile_id: str,
    period: str = Query("30d"),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = _verify_access(profile_id, current_user, db)
    days = {"7d": 7, "30d": 30, "90d": 90}.get(period, 30)
    since = datetime.now(timezone.utc) - timedelta(days=days)

    records = (
        db.query(DiabetesRecord)
        .filter(
            DiabetesRecord.profile_id == profile.id,
            DiabetesRecord.measured_at >= since,
        )
        .order_by(DiabetesRecord.measured_at.desc())
        .all()
    )

    values = [float(r.glucose_value) for r in records]
    fasting = [float(r.glucose_value) for r in records if r.measurement_context == "FASTING"]
    after_meal = [float(r.glucose_value) for r in records if r.measurement_context == "AFTER_MEAL"]

    target_min = float(profile.target_glucose_min or 4.0)
    target_max = float(profile.target_glucose_max or 10.0)
    in_range = [v for v in values if target_min <= v <= target_max]
    in_range_pct = round(len(in_range) / len(values) * 100, 1) if values else None

    glucose_summary = GlucoseSummary(
        period=period,
        total_records=len(records),
        average_glucose=round(sum(values) / len(values), 2) if values else None,
        highest_glucose=max(values) if values else None,
        lowest_glucose=min(values) if values else None,
        fasting_average=round(sum(fasting) / len(fasting), 2) if fasting else None,
        after_meal_average=round(sum(after_meal) / len(after_meal), 2) if after_meal else None,
        in_range_percent=in_range_pct,
    )

    total, taken, missed, skipped = _calc_adherence(db, profile.id, days)
    streak = _streak_days(db, profile.id)

    all_symptoms = [r.symptoms for r in records if r.symptoms]
    symptoms_summary = ", ".join(set(", ".join(all_symptoms).split(","))) if all_symptoms else None

    latest_ai = (
        db.query(DiabetesAiAssessment)
        .filter(DiabetesAiAssessment.profile_id == profile.id)
        .order_by(DiabetesAiAssessment.created_at.desc())
        .first()
    )

    return DiabetesDoctorSummaryResponse(
        period=period,
        profile=DiabetesProfileResponse.model_validate(profile),
        glucose_summary=glucose_summary,
        medication_adherence=DiabetesAdherenceResponse(
            period_days=days,
            total_scheduled=total,
            total_taken=taken,
            total_missed=missed,
            total_skipped=skipped,
            adherence_percent=round((taken / total * 100) if total > 0 else 0.0, 1),
            streak_days=streak,
        ),
        symptoms_summary=symptoms_summary,
        latest_ai_summary=latest_ai.summary if latest_ai else None,
        latest_risk_level=latest_ai.risk_level if latest_ai else None,
        doctor_note=latest_ai.doctor_note if latest_ai else None,
        disclaimer=DISCLAIMER_FULL,
    )
