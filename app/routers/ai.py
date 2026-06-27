from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app.core.database import get_db
from app.core.security import get_current_user
from app.models.user import User
from app.models.elderly import ElderlyProfile
from app.models.health_record import HealthRecord
from app.models.watch_data import WatchData
from app.models.medication import Medication, MedicationLog
from app.models.ai_assessment import AiAssessment
from app.schemas.ai_assessment import AiAssessmentResponse
from app.services.azure_openai_service import call_azure_openai, build_ai_prompt
from app.services.risk_service import rule_based_risk_assessment, max_risk
from app.services.alert_service import create_alert_for_elderly

router = APIRouter(tags=["AI Assessment"])


def verify_elderly_access(elderly_id: str, user: User, db: Session) -> ElderlyProfile:
    profile = db.query(ElderlyProfile).filter(ElderlyProfile.id == elderly_id).first()
    if not profile:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Elderly profile not found")
    if profile.child_user_id != user.id:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Access denied")
    return profile


@router.post("/api/elderly/{elderly_id}/ai-assess", response_model=AiAssessmentResponse)
async def run_ai_assessment(
    elderly_id: str,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = verify_elderly_access(elderly_id, current_user, db)

    latest_health = (
        db.query(HealthRecord)
        .filter(HealthRecord.elderly_id == elderly_id)
        .order_by(HealthRecord.created_at.desc())
        .first()
    )

    latest_watch = (
        db.query(WatchData)
        .filter(WatchData.elderly_id == elderly_id)
        .order_by(WatchData.recorded_at.desc())
        .first()
    )

    total_meds = (
        db.query(Medication)
        .filter(Medication.elderly_id == elderly_id, Medication.active == True)
        .count()
    )
    taken_today = (
        db.query(MedicationLog)
        .filter(MedicationLog.elderly_id == elderly_id, MedicationLog.taken == True)
        .count()
    )
    med_status = f"{taken_today}/{total_meds} dori qabul qilingan" if total_meds > 0 else "Dori tayinlanmagan"

    health_dict = None
    if latest_health:
        health_dict = {
            "systolic_bp": latest_health.systolic_bp,
            "diastolic_bp": latest_health.diastolic_bp,
            "blood_sugar": float(latest_health.blood_sugar) if latest_health.blood_sugar else None,
            "heart_rate": latest_health.heart_rate,
            "spo2": latest_health.spo2,
            "temperature": float(latest_health.temperature) if latest_health.temperature else None,
            "symptoms": latest_health.symptoms,
            "mood": latest_health.mood,
        }

    watch_dict = None
    if latest_watch:
        watch_dict = {
            "heart_rate": latest_watch.heart_rate,
            "steps": latest_watch.steps,
            "sleep_hours": float(latest_watch.sleep_hours) if latest_watch.sleep_hours else None,
            "spo2": latest_watch.spo2,
            "fall_detected": latest_watch.fall_detected,
            "battery": latest_watch.battery,
        }

    rule_result = rule_based_risk_assessment(latest_health, latest_watch)

    user_prompt = build_ai_prompt(
        elderly_name=profile.full_name,
        elderly_age=profile.age,
        chronic_diseases=profile.chronic_diseases,
        health_record=health_dict,
        watch_data=watch_dict,
        medication_status=med_status,
    )

    ai_result = await call_azure_openai(user_prompt)

    if ai_result:
        final_risk = max_risk(rule_result["risk_level"], ai_result["risk_level"])
        source = "azure_openai"
        summary = ai_result["summary"]
        recommendation = ai_result["recommendation"]
        raw_response = str(ai_result)
    else:
        final_risk = rule_result["risk_level"]
        source = "rule_based"
        summary = rule_result["summary"]
        recommendation = rule_result["recommendation"]
        raw_response = None

    assessment = AiAssessment(
        elderly_id=elderly_id,
        source=source,
        risk_level=final_risk,
        summary=summary,
        recommendation=recommendation,
        raw_ai_response=raw_response,
    )
    db.add(assessment)
    db.commit()
    db.refresh(assessment)

    if final_risk in ("HIGH", "EMERGENCY"):
        severity = "EMERGENCY" if final_risk == "EMERGENCY" else "HIGH"
        create_alert_for_elderly(
            db=db,
            elderly_id=elderly_id,
            alert_type="AI_RISK",
            title=f"AI xavf baholash: {final_risk}",
            message=summary,
            severity=severity,
        )

    return assessment


@router.get("/api/elderly/{elderly_id}/ai-assessments", response_model=List[AiAssessmentResponse])
def list_ai_assessments(
    elderly_id: str,
    limit: int = 10,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    verify_elderly_access(elderly_id, current_user, db)
    return (
        db.query(AiAssessment)
        .filter(AiAssessment.elderly_id == elderly_id)
        .order_by(AiAssessment.created_at.desc())
        .limit(limit)
        .all()
    )


@router.get("/api/elderly/{elderly_id}/ai-assessments/latest", response_model=AiAssessmentResponse)
def get_latest_ai_assessment(
    elderly_id: str,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    verify_elderly_access(elderly_id, current_user, db)
    assessment = (
        db.query(AiAssessment)
        .filter(AiAssessment.elderly_id == elderly_id)
        .order_by(AiAssessment.created_at.desc())
        .first()
    )
    if not assessment:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="No AI assessments found")
    return assessment
