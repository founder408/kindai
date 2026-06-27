"""
Diabetes self-service endpoints: /api/diabetes/me/*
All endpoints require authenticated user with a DiabetesProfile.
"""
import json
import uuid
from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session
from sqlalchemy import func, cast, Date
from typing import List, Optional
from datetime import datetime, timezone, timedelta, date

from app.core.database import get_db
from app.core.security import get_current_user
from app.models.user import User
from app.models.diabetes_profile import DiabetesProfile
from app.models.diabetes_record import DiabetesRecord
from app.models.diabetes_medication import DiabetesMedication, DiabetesMedicationLog
from app.models.diabetes_ai_assessment import DiabetesAiAssessment
from app.models.diabetes_alert import DiabetesAlert
from app.schemas.diabetes import (
    DiabetesProfileCreate, DiabetesProfileUpdate, DiabetesProfileResponse,
    DiabetesRecordCreate, DiabetesRecordResponse,
    GlucoseTrendsResponse,
    DiabetesMedicationCreate, DiabetesMedicationUpdate, DiabetesMedicationResponse,
    DiabetesMedicationLogResponse, MedicationTakenRequest, DiabetesAdherenceResponse,
    DiabetesAiAssessmentResponse,
    DiabetesAlertResponse,
    SosRequest, SosResponse,
    DiabetesDashboardResponse, MedicationAdherenceSummary, WeeklySummary,
    DiabetesDoctorSummaryResponse, GlucoseSummary,
)
from app.services.diabetes_ai_service import call_diabetes_ai, build_diabetes_prompt
from app.services.diabetes_risk_service import (
    diabetes_rule_based_assessment, determine_trend_direction, max_risk,
    DISCLAIMER,
)

router = APIRouter(prefix="/api/diabetes/me", tags=["Diabetes Self-Service"])

DISCLAIMER_FULL = (
    "Bu tibbiy tashxis emas. Zarur holatda shifokorga murojaat qiling. "
    "Kind AI dori yoki insulin dozasini belgilamaydi."
)


# ── Helper ────────────────────────────────────────────────────────────────────

def get_my_diabetes_profile(user: User, db: Session) -> DiabetesProfile:
    profile = db.query(DiabetesProfile).filter(
        DiabetesProfile.user_id == user.id
    ).first()
    if not profile:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Diabetes profili topilmadi. Avval profil yarating.",
        )
    return profile


def _calc_adherence(
    db: Session, profile_id: str, days: int
) -> tuple[int, int, int, int]:
    """Returns (total_scheduled, taken, missed, skipped)."""
    since = datetime.now(timezone.utc) - timedelta(days=days)
    logs = (
        db.query(DiabetesMedicationLog)
        .filter(
            DiabetesMedicationLog.profile_id == profile_id,
            DiabetesMedicationLog.scheduled_time >= since,
        )
        .all()
    )
    taken = sum(1 for l in logs if l.status == "TAKEN")
    missed = sum(1 for l in logs if l.status == "MISSED")
    skipped = sum(1 for l in logs if l.status == "SKIPPED")
    return len(logs), taken, missed, skipped


def _streak_days(db: Session, profile_id: str) -> int:
    """Count consecutive days where adherence == 100%."""
    streak = 0
    today = date.today()
    for i in range(30):
        check_date = today - timedelta(days=i)
        logs = (
            db.query(DiabetesMedicationLog)
            .filter(
                DiabetesMedicationLog.profile_id == profile_id,
                cast(DiabetesMedicationLog.scheduled_time, Date) == check_date,
            )
            .all()
        )
        if not logs:
            break
        if all(l.status == "TAKEN" for l in logs):
            streak += 1
        else:
            break
    return streak


def _create_diabetes_alert(
    db: Session,
    profile_id: str,
    user_id: str,
    alert_type: str,
    title: str,
    message: str,
    severity: str,
) -> DiabetesAlert:
    alert = DiabetesAlert(
        id=str(uuid.uuid4()),
        profile_id=profile_id,
        user_id=user_id,
        type=alert_type,
        title=title,
        message=message,
        severity=severity,
    )
    db.add(alert)
    db.commit()
    db.refresh(alert)
    return alert


# ── Profile ───────────────────────────────────────────────────────────────────

@router.post("/profile", response_model=DiabetesProfileResponse, status_code=status.HTTP_201_CREATED)
def create_my_profile(
    body: DiabetesProfileCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    existing = db.query(DiabetesProfile).filter(
        DiabetesProfile.user_id == current_user.id
    ).first()
    if existing:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Diabetes profili allaqachon mavjud.",
        )
    profile = DiabetesProfile(
        id=str(uuid.uuid4()),
        user_id=current_user.id,
        **body.model_dump(),
    )
    db.add(profile)
    db.commit()
    db.refresh(profile)
    return profile


@router.get("/profile", response_model=DiabetesProfileResponse)
def get_my_profile(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    return get_my_diabetes_profile(current_user, db)


@router.put("/profile", response_model=DiabetesProfileResponse)
def update_my_profile(
    body: DiabetesProfileUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_diabetes_profile(current_user, db)
    for field, value in body.model_dump(exclude_unset=True).items():
        setattr(profile, field, value)
    db.commit()
    db.refresh(profile)
    return profile


# ── Dashboard ─────────────────────────────────────────────────────────────────

@router.get("/dashboard", response_model=DiabetesDashboardResponse)
def get_my_dashboard(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_diabetes_profile(current_user, db)
    today_start = datetime.now(timezone.utc).replace(hour=0, minute=0, second=0, microsecond=0)

    # Today's first glucose record
    today_glucose = (
        db.query(DiabetesRecord)
        .filter(
            DiabetesRecord.profile_id == profile.id,
            DiabetesRecord.measured_at >= today_start,
        )
        .order_by(DiabetesRecord.measured_at.asc())
        .first()
    )

    # Latest glucose (any time)
    latest_glucose = (
        db.query(DiabetesRecord)
        .filter(DiabetesRecord.profile_id == profile.id)
        .order_by(DiabetesRecord.measured_at.desc())
        .first()
    )

    # Today's active medications
    today_meds = (
        db.query(DiabetesMedication)
        .filter(
            DiabetesMedication.profile_id == profile.id,
            DiabetesMedication.is_active == True,
        )
        .all()
    )

    # Medication adherence (7 days)
    total, taken, missed, skipped = _calc_adherence(db, profile.id, 7)
    adherence = MedicationAdherenceSummary(
        total_scheduled=total,
        total_taken=taken,
        adherence_percent=round((taken / total * 100) if total > 0 else 0.0, 1),
    )

    # Latest AI assessment
    latest_ai = (
        db.query(DiabetesAiAssessment)
        .filter(DiabetesAiAssessment.profile_id == profile.id)
        .order_by(DiabetesAiAssessment.created_at.desc())
        .first()
    )

    # Unread alerts
    unread = (
        db.query(DiabetesAlert)
        .filter(
            DiabetesAlert.profile_id == profile.id,
            DiabetesAlert.is_read == False,
        )
        .count()
    )

    # Weekly summary
    week_ago = datetime.now(timezone.utc) - timedelta(days=7)
    week_records = (
        db.query(DiabetesRecord)
        .filter(
            DiabetesRecord.profile_id == profile.id,
            DiabetesRecord.measured_at >= week_ago,
        )
        .order_by(DiabetesRecord.measured_at.desc())
        .all()
    )
    weekly = None
    if week_records:
        vals = [float(r.glucose_value) for r in week_records]
        weekly = WeeklySummary(
            average_glucose=round(sum(vals) / len(vals), 2),
            highest_glucose=max(vals),
            lowest_glucose=min(vals),
            trend_direction=determine_trend_direction(week_records),
            total_records=len(week_records),
        )

    ai_response = None
    if latest_ai:
        ai_response = _parse_ai_assessment(latest_ai)

    return DiabetesDashboardResponse(
        profile=profile,
        today_glucose=today_glucose,
        latest_glucose=latest_glucose,
        today_medications=today_meds,
        medication_adherence=adherence,
        latest_ai_assessment=ai_response,
        unread_alerts=unread,
        weekly_summary=weekly,
    )


# ── Glucose Records ───────────────────────────────────────────────────────────

@router.post("/glucose", response_model=DiabetesRecordResponse, status_code=status.HTTP_201_CREATED)
def add_glucose_record(
    body: DiabetesRecordCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_diabetes_profile(current_user, db)
    record = DiabetesRecord(
        id=str(uuid.uuid4()),
        profile_id=profile.id,
        user_id=current_user.id,
        **body.model_dump(),
    )
    db.add(record)
    db.commit()
    db.refresh(record)

    # Auto-alert for extreme values
    glucose = float(record.glucose_value)
    if glucose < 3.0:
        _create_diabetes_alert(
            db, profile.id, current_user.id,
            "LOW_GLUCOSE",
            "Qand ko'rsatkichi past signal",
            f"Qand ko'rsatkichi {glucose} mmol/L ({record.measurement_context}). Ehtiyot bo'ling.",
            "EMERGENCY",
        )
    elif glucose >= 16.7:
        _create_diabetes_alert(
            db, profile.id, current_user.id,
            "HIGH_GLUCOSE",
            "Qand ko'rsatkichi yuqori signal",
            f"Qand ko'rsatkichi {glucose} mmol/L ({record.measurement_context}). Shifokor bilan maslahatlashing.",
            "HIGH",
        )

    return record


@router.get("/glucose", response_model=List[DiabetesRecordResponse])
def list_glucose_records(
    period: str = Query("7d", description="7d | 30d | 90d | all"),
    context: Optional[str] = Query(None, description="FASTING|BEFORE_MEAL|AFTER_MEAL|BEDTIME|RANDOM"),
    limit: int = Query(50, le=200),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_diabetes_profile(current_user, db)
    query = db.query(DiabetesRecord).filter(DiabetesRecord.profile_id == profile.id)

    if period != "all":
        days = {"7d": 7, "30d": 30, "90d": 90}.get(period, 7)
        since = datetime.now(timezone.utc) - timedelta(days=days)
        query = query.filter(DiabetesRecord.measured_at >= since)

    if context:
        query = query.filter(DiabetesRecord.measurement_context == context.upper())

    return query.order_by(DiabetesRecord.measured_at.desc()).limit(limit).all()


@router.get("/glucose/trends", response_model=GlucoseTrendsResponse)
def get_glucose_trends(
    period: str = Query("7d", description="7d | 30d"),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_diabetes_profile(current_user, db)
    days = {"7d": 7, "30d": 30}.get(period, 7)
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

    if not records:
        return GlucoseTrendsResponse(period=period, total_records=0)

    values = [float(r.glucose_value) for r in records]
    fasting = [float(r.glucose_value) for r in records if r.measurement_context == "FASTING"]
    after_meal = [float(r.glucose_value) for r in records if r.measurement_context == "AFTER_MEAL"]
    before_meal = [float(r.glucose_value) for r in records if r.measurement_context == "BEFORE_MEAL"]
    bedtime = [float(r.glucose_value) for r in records if r.measurement_context == "BEDTIME"]

    # Group by date for daily_data
    daily_map: dict = {}
    for r in records:
        d = r.measured_at.date().isoformat()
        daily_map.setdefault(d, []).append(float(r.glucose_value))
    daily_data = [
        {"date": d, "avg": round(sum(v) / len(v), 2), "count": len(v)}
        for d, v in sorted(daily_map.items())
    ]

    return GlucoseTrendsResponse(
        period=period,
        total_records=len(records),
        average_glucose=round(sum(values) / len(values), 2),
        highest_glucose=max(values),
        lowest_glucose=min(values),
        fasting_average=round(sum(fasting) / len(fasting), 2) if fasting else None,
        after_meal_average=round(sum(after_meal) / len(after_meal), 2) if after_meal else None,
        before_meal_average=round(sum(before_meal) / len(before_meal), 2) if before_meal else None,
        bedtime_average=round(sum(bedtime) / len(bedtime), 2) if bedtime else None,
        trend_direction=determine_trend_direction(records),
        daily_data=daily_data,
    )


# ── Medications ───────────────────────────────────────────────────────────────

@router.get("/medications", response_model=List[DiabetesMedicationResponse])
def list_medications(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_diabetes_profile(current_user, db)
    return (
        db.query(DiabetesMedication)
        .filter(
            DiabetesMedication.profile_id == profile.id,
            DiabetesMedication.is_active == True,
        )
        .order_by(DiabetesMedication.schedule_time)
        .all()
    )


@router.post("/medications", response_model=DiabetesMedicationResponse, status_code=status.HTTP_201_CREATED)
def add_medication(
    body: DiabetesMedicationCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_diabetes_profile(current_user, db)
    med = DiabetesMedication(
        id=str(uuid.uuid4()),
        profile_id=profile.id,
        created_by_user_id=current_user.id,
        **body.model_dump(),
    )
    db.add(med)
    db.commit()
    db.refresh(med)
    return med


@router.put("/medications/{medication_id}", response_model=DiabetesMedicationResponse)
def update_medication(
    medication_id: str,
    body: DiabetesMedicationUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_diabetes_profile(current_user, db)
    med = db.query(DiabetesMedication).filter(
        DiabetesMedication.id == medication_id,
        DiabetesMedication.profile_id == profile.id,
    ).first()
    if not med:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Dori topilmadi")
    for field, value in body.model_dump(exclude_unset=True).items():
        setattr(med, field, value)
    db.commit()
    db.refresh(med)
    return med


@router.delete("/medications/{medication_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_medication(
    medication_id: str,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_diabetes_profile(current_user, db)
    med = db.query(DiabetesMedication).filter(
        DiabetesMedication.id == medication_id,
        DiabetesMedication.profile_id == profile.id,
    ).first()
    if not med:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Dori topilmadi")
    med.is_active = False  # soft delete
    db.commit()


@router.post("/medications/{medication_id}/taken", response_model=DiabetesMedicationLogResponse)
def mark_medication_taken(
    medication_id: str,
    body: MedicationTakenRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_diabetes_profile(current_user, db)
    med = db.query(DiabetesMedication).filter(
        DiabetesMedication.id == medication_id,
        DiabetesMedication.profile_id == profile.id,
    ).first()
    if not med:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Dori topilmadi")

    log = DiabetesMedicationLog(
        id=str(uuid.uuid4()),
        medication_id=medication_id,
        profile_id=profile.id,
        taken_by_user_id=current_user.id,
        scheduled_time=body.scheduled_time,
        status=body.status.upper(),
        taken_at=datetime.now(timezone.utc) if body.status == "TAKEN" else None,
        note=body.note,
    )
    db.add(log)
    db.commit()
    db.refresh(log)

    if body.status == "MISSED":
        _create_diabetes_alert(
            db, profile.id, current_user.id,
            "MISSED_MEDICATION",
            "Dori qabul qilish o'tkazib yuborildi",
            f"{med.name} — {body.scheduled_time.strftime('%H:%M')} da qabul qilinmadi.",
            "MEDIUM",
        )

    return log


@router.get("/medication-adherence", response_model=DiabetesAdherenceResponse)
def get_medication_adherence(
    days: int = Query(7, le=90),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_diabetes_profile(current_user, db)
    total, taken, missed, skipped = _calc_adherence(db, profile.id, days)
    streak = _streak_days(db, profile.id)
    return DiabetesAdherenceResponse(
        period_days=days,
        total_scheduled=total,
        total_taken=taken,
        total_missed=missed,
        total_skipped=skipped,
        adherence_percent=round((taken / total * 100) if total > 0 else 0.0, 1),
        streak_days=streak,
    )


# ── AI Assessment ─────────────────────────────────────────────────────────────

def _parse_ai_assessment(record: DiabetesAiAssessment) -> DiabetesAiAssessmentResponse:
    def _parse_list(field: Optional[str]) -> Optional[List[str]]:
        if not field:
            return None
        try:
            parsed = json.loads(field)
            return parsed if isinstance(parsed, list) else [str(parsed)]
        except Exception:
            return [field]

    return DiabetesAiAssessmentResponse(
        id=record.id,
        profile_id=record.profile_id,
        source=record.source,
        risk_level=record.risk_level,
        summary=record.summary,
        key_concerns=_parse_list(record.key_concerns),
        recommended_next_steps=_parse_list(record.recommended_next_steps),
        what_to_monitor=_parse_list(record.what_to_monitor),
        doctor_note=record.doctor_note,
        disclaimer=record.disclaimer,
        created_at=record.created_at,
    )


@router.post("/ai-assess", response_model=DiabetesAiAssessmentResponse)
async def run_ai_assessment(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_diabetes_profile(current_user, db)

    # Collect last 10 glucose records (7 days)
    week_ago = datetime.now(timezone.utc) - timedelta(days=7)
    glucose_records = (
        db.query(DiabetesRecord)
        .filter(
            DiabetesRecord.profile_id == profile.id,
            DiabetesRecord.measured_at >= week_ago,
        )
        .order_by(DiabetesRecord.measured_at.desc())
        .limit(10)
        .all()
    )

    # Medication adherence (7 days)
    total, taken, missed, skipped = _calc_adherence(db, profile.id, 7)
    adherence_pct = round((taken / total * 100) if total > 0 else 0.0, 1)
    missed_7d = missed

    # Recent symptoms from latest record
    symptoms_recent = glucose_records[0].symptoms if glucose_records else None

    # Previous AI risk
    prev_assessment = (
        db.query(DiabetesAiAssessment)
        .filter(DiabetesAiAssessment.profile_id == profile.id)
        .order_by(DiabetesAiAssessment.created_at.desc())
        .first()
    )
    prev_risk = prev_assessment.risk_level if prev_assessment else None

    # Build AI prompt
    glucose_dicts = [
        {
            "glucose_value": float(r.glucose_value),
            "unit": r.unit,
            "measurement_context": r.measurement_context,
            "measured_at": r.measured_at.isoformat(),
            "symptoms": r.symptoms,
        }
        for r in glucose_records
    ]

    user_prompt = build_diabetes_prompt(
        profile_name=profile.full_name,
        profile_age=profile.age,
        diabetes_type=profile.diabetes_type,
        glucose_records=glucose_dicts,
        med_adherence_percent=adherence_pct,
        missed_doses_7d=missed_7d,
        symptoms_recent=symptoms_recent,
        previous_risk=prev_risk,
    )

    # Try Azure OpenAI first
    ai_result = await call_diabetes_ai(user_prompt)

    if ai_result:
        source = "azure_openai"
        risk_level = ai_result["risk_level"]
        summary = ai_result["summary"]
        key_concerns = json.dumps(ai_result["key_concerns"], ensure_ascii=False)
        next_steps = json.dumps(ai_result["recommended_next_steps"], ensure_ascii=False)
        what_to_monitor = json.dumps(ai_result["what_to_monitor"], ensure_ascii=False)
        doctor_note = ai_result.get("doctor_note", "")
    else:
        # Rule-based fallback
        source = "rule_based"
        rule = diabetes_rule_based_assessment(
            latest_records=glucose_records,
            profile=profile,
            med_adherence_percent=adherence_pct,
            missed_doses_7d=missed_7d,
            symptoms_list=symptoms_recent.split(",") if symptoms_recent else [],
        )
        risk_level = rule["risk_level"]
        summary = rule["summary"]
        key_concerns = json.dumps(rule["key_concerns"], ensure_ascii=False)
        next_steps = json.dumps(rule["recommended_next_steps"], ensure_ascii=False)
        what_to_monitor = json.dumps(rule["what_to_monitor"], ensure_ascii=False)
        doctor_note = rule.get("doctor_note", "")

    assessment = DiabetesAiAssessment(
        id=str(uuid.uuid4()),
        profile_id=profile.id,
        user_id=current_user.id,
        source=source,
        risk_level=risk_level,
        summary=summary,
        key_concerns=key_concerns,
        recommended_next_steps=next_steps,
        what_to_monitor=what_to_monitor,
        doctor_note=doctor_note,
        disclaimer=DISCLAIMER_FULL,
    )
    db.add(assessment)
    db.commit()
    db.refresh(assessment)

    # Alert on HIGH / EMERGENCY
    if risk_level in ("HIGH", "EMERGENCY"):
        severity = "EMERGENCY" if risk_level == "EMERGENCY" else "HIGH"
        _create_diabetes_alert(
            db, profile.id, current_user.id,
            "AI_RISK",
            f"AI tahlil: {risk_level} xavf signali",
            summary,
            severity,
        )

    return _parse_ai_assessment(assessment)


@router.get("/ai-assessments", response_model=List[DiabetesAiAssessmentResponse])
def list_ai_assessments(
    limit: int = Query(10, le=50),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_diabetes_profile(current_user, db)
    records = (
        db.query(DiabetesAiAssessment)
        .filter(DiabetesAiAssessment.profile_id == profile.id)
        .order_by(DiabetesAiAssessment.created_at.desc())
        .limit(limit)
        .all()
    )
    return [_parse_ai_assessment(r) for r in records]


# ── Alerts ────────────────────────────────────────────────────────────────────

@router.get("/alerts", response_model=List[DiabetesAlertResponse])
def get_alerts(
    unread_only: bool = Query(False),
    limit: int = Query(20, le=100),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_diabetes_profile(current_user, db)
    query = db.query(DiabetesAlert).filter(DiabetesAlert.profile_id == profile.id)
    if unread_only:
        query = query.filter(DiabetesAlert.is_read == False)
    return query.order_by(DiabetesAlert.created_at.desc()).limit(limit).all()


@router.post("/alerts/{alert_id}/read", status_code=status.HTTP_200_OK)
def mark_alert_read(
    alert_id: str,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_diabetes_profile(current_user, db)
    alert = db.query(DiabetesAlert).filter(
        DiabetesAlert.id == alert_id,
        DiabetesAlert.profile_id == profile.id,
    ).first()
    if not alert:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Xabar topilmadi")
    alert.is_read = True
    db.commit()
    return {"message": "O'qildi"}


@router.post("/sos", response_model=SosResponse)
def send_sos(
    body: SosRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_diabetes_profile(current_user, db)
    msg = body.message
    if body.location:
        msg += f" | Joylashuv: {body.location}"

    alert = _create_diabetes_alert(
        db, profile.id, current_user.id,
        "SOS",
        "Yaqiningiz yordam so'radi",
        msg,
        "EMERGENCY",
    )
    return SosResponse(
        alert_id=alert.id,
        message="SOS signali yuborildi. Yaqiningizga xabar ketdi.",
        emergency_phone=profile.emergency_phone,
    )


# ── Doctor Summary ────────────────────────────────────────────────────────────

@router.get("/doctor-summary", response_model=DiabetesDoctorSummaryResponse)
def get_doctor_summary(
    period: str = Query("30d", description="7d | 30d | 90d"),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_diabetes_profile(current_user, db)
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
    adherence = DiabetesAdherenceResponse(
        period_days=days,
        total_scheduled=total,
        total_taken=taken,
        total_missed=missed,
        total_skipped=skipped,
        adherence_percent=round((taken / total * 100) if total > 0 else 0.0, 1),
        streak_days=streak,
    )

    # All symptoms from period
    all_symptoms = [r.symptoms for r in records if r.symptoms]
    symptoms_summary = ", ".join(set(", ".join(all_symptoms).split(","))) if all_symptoms else None

    # Latest AI
    latest_ai = (
        db.query(DiabetesAiAssessment)
        .filter(DiabetesAiAssessment.profile_id == profile.id)
        .order_by(DiabetesAiAssessment.created_at.desc())
        .first()
    )

    return DiabetesDoctorSummaryResponse(
        period=period,
        profile=profile,
        glucose_summary=glucose_summary,
        medication_adherence=adherence,
        symptoms_summary=symptoms_summary,
        latest_ai_summary=latest_ai.summary if latest_ai else None,
        latest_risk_level=latest_ai.risk_level if latest_ai else None,
        doctor_note=latest_ai.doctor_note if latest_ai else None,
        disclaimer=DISCLAIMER_FULL,
    )
