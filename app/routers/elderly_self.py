from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from sqlalchemy import func, cast, Date
from typing import List, Optional
from datetime import datetime, timezone, timedelta, date
from pydantic import BaseModel
from app.core.database import get_db
from app.core.security import get_current_user
from app.models.user import User
from app.models.elderly import ElderlyProfile
from app.models.health_record import HealthRecord
from app.models.medication import Medication, MedicationLog
from app.models.ai_assessment import AiAssessment
from app.models.alert import Alert
from app.models.watch_data import WatchData
from app.schemas.elderly import ElderlyResponse
from app.schemas.health_record import HealthRecordCreate, HealthRecordResponse
from app.schemas.medication import (
    MedicationCreate, MedicationUpdate, MedicationResponse,
    MedicationTakenRequest, MedicationLogResponse
)
from app.schemas.ai_assessment import AiAssessmentResponse
from app.schemas.alert import AlertResponse
from app.services.azure_openai_service import call_azure_openai, build_ai_prompt
from app.services.risk_service import rule_based_risk_assessment, max_risk
from app.services.alert_service import create_alert_for_elderly

router = APIRouter(prefix="/api/elderly/me", tags=["Elderly Self-Service"])


def get_my_profile(user: User, db: Session) -> ElderlyProfile:
    if user.role != "ELDERLY":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Only elderly users can access this endpoint",
        )
    profile = db.query(ElderlyProfile).filter(
        ElderlyProfile.elderly_user_id == user.id
    ).first()
    if not profile:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="No elderly profile linked to this account",
        )
    return profile


# ── Profile ──────────────────────────────────────────────────────────────────

@router.get("/profile", response_model=ElderlyResponse)
def get_my_profile_endpoint(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    return get_my_profile(current_user, db)


@router.post("/consent", status_code=status.HTTP_200_OK)
def confirm_consent(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_profile(current_user, db)
    profile.consent_confirmed = True
    profile.consent_confirmed_at = datetime.now(timezone.utc)
    db.commit()
    return {"message": "Consent confirmed"}


# ── Dashboard ─────────────────────────────────────────────────────────────────

class ElderlyDashboardResponse(BaseModel):
    profile: ElderlyResponse
    latest_health: Optional[HealthRecordResponse] = None
    unread_alerts: int = 0
    total_meds: int = 0
    taken_today: int = 0
    latest_risk_level: Optional[str] = None

    model_config = {"from_attributes": True}


@router.get("/dashboard", response_model=ElderlyDashboardResponse)
def get_my_dashboard(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_profile(current_user, db)

    latest_health = (
        db.query(HealthRecord)
        .filter(HealthRecord.elderly_id == profile.id)
        .order_by(HealthRecord.created_at.desc())
        .first()
    )

    unread_alerts = (
        db.query(Alert)
        .filter(Alert.elderly_id == profile.id, Alert.is_read == False)
        .count()
    )

    total_meds = (
        db.query(Medication)
        .filter(Medication.elderly_id == profile.id, Medication.active == True)
        .count()
    )

    today_start = datetime.now(timezone.utc).replace(hour=0, minute=0, second=0, microsecond=0)
    taken_today = (
        db.query(MedicationLog)
        .filter(
            MedicationLog.elderly_id == profile.id,
            MedicationLog.taken == True,
            MedicationLog.taken_at >= today_start,
        )
        .count()
    )

    latest_assessment = (
        db.query(AiAssessment)
        .filter(AiAssessment.elderly_id == profile.id)
        .order_by(AiAssessment.created_at.desc())
        .first()
    )

    return ElderlyDashboardResponse(
        profile=ElderlyResponse.model_validate(profile),
        latest_health=HealthRecordResponse.model_validate(latest_health) if latest_health else None,
        unread_alerts=unread_alerts,
        total_meds=total_meds,
        taken_today=taken_today,
        latest_risk_level=latest_assessment.risk_level if latest_assessment else None,
    )


# ── Health Records ────────────────────────────────────────────────────────────

@router.post("/health-records", response_model=HealthRecordResponse, status_code=status.HTTP_201_CREATED)
def create_my_health_record(
    body: HealthRecordCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_profile(current_user, db)
    record = HealthRecord(elderly_id=profile.id, **body.model_dump())
    db.add(record)
    db.commit()
    db.refresh(record)
    return record


@router.get("/health-records", response_model=List[HealthRecordResponse])
def get_my_health_records(
    limit: int = 20,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_profile(current_user, db)
    return (
        db.query(HealthRecord)
        .filter(HealthRecord.elderly_id == profile.id)
        .order_by(HealthRecord.created_at.desc())
        .limit(limit)
        .all()
    )


# ── Health Trends ─────────────────────────────────────────────────────────────

class TrendPoint(BaseModel):
    date: str
    systolic_bp: Optional[float] = None
    diastolic_bp: Optional[float] = None
    heart_rate: Optional[float] = None
    blood_sugar: Optional[float] = None
    spo2: Optional[float] = None


class HealthTrendsResponse(BaseModel):
    period: str
    points: List[TrendPoint]


@router.get("/health-trends", response_model=HealthTrendsResponse)
def get_my_health_trends(
    period: str = "weekly",
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_profile(current_user, db)

    if period == "monthly":
        days_back = 30
    elif period == "daily":
        days_back = 7
    else:
        days_back = 7

    cutoff = datetime.now(timezone.utc) - timedelta(days=days_back)

    records = (
        db.query(HealthRecord)
        .filter(
            HealthRecord.elderly_id == profile.id,
            HealthRecord.created_at >= cutoff,
        )
        .order_by(HealthRecord.created_at.asc())
        .all()
    )

    points = []
    for r in records:
        points.append(TrendPoint(
            date=r.created_at.strftime("%Y-%m-%d"),
            systolic_bp=r.systolic_bp,
            diastolic_bp=r.diastolic_bp,
            heart_rate=r.heart_rate,
            blood_sugar=float(r.blood_sugar) if r.blood_sugar else None,
            spo2=r.spo2,
        ))

    return HealthTrendsResponse(period=period, points=points)


# ── Medications ───────────────────────────────────────────────────────────────

@router.get("/medications", response_model=List[MedicationResponse])
def get_my_medications(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_profile(current_user, db)
    return (
        db.query(Medication)
        .filter(Medication.elderly_id == profile.id, Medication.active == True)
        .order_by(Medication.time_to_take.asc())
        .all()
    )


@router.post("/medications", response_model=MedicationResponse, status_code=status.HTTP_201_CREATED)
def add_my_medication(
    body: MedicationCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_profile(current_user, db)
    med = Medication(elderly_id=profile.id, **body.model_dump())
    db.add(med)
    db.commit()
    db.refresh(med)
    return med


@router.put("/medications/{medication_id}", response_model=MedicationResponse)
def update_my_medication(
    medication_id: str,
    body: MedicationUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_profile(current_user, db)
    med = db.query(Medication).filter(
        Medication.id == medication_id,
        Medication.elderly_id == profile.id
    ).first()
    if not med:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Medication not found")
    for key, value in body.model_dump(exclude_unset=True).items():
        setattr(med, key, value)
    db.commit()
    db.refresh(med)
    return med


@router.delete("/medications/{medication_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_my_medication(
    medication_id: str,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_profile(current_user, db)
    med = db.query(Medication).filter(
        Medication.id == medication_id,
        Medication.elderly_id == profile.id
    ).first()
    if not med:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Medication not found")
    med.active = False
    db.commit()


@router.post("/medications/{medication_id}/taken", response_model=MedicationLogResponse)
def mark_medication_taken(
    medication_id: str,
    body: MedicationTakenRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_profile(current_user, db)
    med = db.query(Medication).filter(
        Medication.id == medication_id,
        Medication.elderly_id == profile.id
    ).first()
    if not med:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Medication not found")

    # Avoid double-logging for same scheduled_time
    existing = db.query(MedicationLog).filter(
        MedicationLog.medication_id == medication_id,
        MedicationLog.scheduled_time == body.scheduled_time,
    ).first()
    if existing:
        existing.taken = True
        existing.taken_at = datetime.now(timezone.utc)
        db.commit()
        db.refresh(existing)
        return existing

    log = MedicationLog(
        medication_id=medication_id,
        elderly_id=profile.id,
        scheduled_time=body.scheduled_time,
        taken=True,
        taken_at=datetime.now(timezone.utc),
    )
    db.add(log)
    db.commit()
    db.refresh(log)
    return log


# ── Medication Adherence ──────────────────────────────────────────────────────

class AdherenceResponse(BaseModel):
    period_days: int
    total_scheduled: int
    total_taken: int
    adherence_percent: float


@router.get("/medication-adherence", response_model=AdherenceResponse)
def get_my_medication_adherence(
    days: int = 7,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_profile(current_user, db)
    cutoff = datetime.now(timezone.utc) - timedelta(days=days)

    total = (
        db.query(MedicationLog)
        .filter(
            MedicationLog.elderly_id == profile.id,
            MedicationLog.created_at >= cutoff,
        )
        .count()
    )
    taken = (
        db.query(MedicationLog)
        .filter(
            MedicationLog.elderly_id == profile.id,
            MedicationLog.taken == True,
            MedicationLog.created_at >= cutoff,
        )
        .count()
    )

    return AdherenceResponse(
        period_days=days,
        total_scheduled=total,
        total_taken=taken,
        adherence_percent=round(taken / total * 100, 1) if total > 0 else 100.0,
    )


# ── AI Assessment (self) ──────────────────────────────────────────────────────

@router.post("/ai-assess", response_model=AiAssessmentResponse)
async def run_my_ai_assessment(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_profile(current_user, db)

    latest_health = (
        db.query(HealthRecord)
        .filter(HealthRecord.elderly_id == profile.id)
        .order_by(HealthRecord.created_at.desc())
        .first()
    )

    latest_watch = (
        db.query(WatchData)
        .filter(WatchData.elderly_id == profile.id)
        .order_by(WatchData.recorded_at.desc())
        .first()
    )

    total_meds = (
        db.query(Medication)
        .filter(Medication.elderly_id == profile.id, Medication.active == True)
        .count()
    )
    today_start = datetime.now(timezone.utc).replace(hour=0, minute=0, second=0, microsecond=0)
    taken_today = (
        db.query(MedicationLog)
        .filter(
            MedicationLog.elderly_id == profile.id,
            MedicationLog.taken == True,
            MedicationLog.taken_at >= today_start,
        )
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

    # Generate elderly-friendly summary
    elderly_summary_map = {
        "LOW": "Sog'lig'ingiz yaxshi. Dori ichishni davom ettiring.",
        "MEDIUM": "Ba'zi ko'rsatkichlar e'tibor talab qiladi. Farzandingizga ayting.",
        "HIGH": "Sog'lig'ingizda muammo bor. Zudlik bilan farzandingizni xabardor qiling.",
        "EMERGENCY": "Favqulodda holat! Darhol 103 ga qo'ng'iroq qiling yoki farzandingizni chaqiring.",
    }
    elderly_summary = elderly_summary_map.get(final_risk, summary)

    assessment = AiAssessment(
        elderly_id=profile.id,
        source=source,
        risk_level=final_risk,
        summary=summary,
        recommendation=recommendation,
        elderly_summary=elderly_summary,
        raw_ai_response=raw_response,
    )
    db.add(assessment)
    db.commit()
    db.refresh(assessment)

    if final_risk in ("HIGH", "EMERGENCY"):
        severity = "EMERGENCY" if final_risk == "EMERGENCY" else "HIGH"
        create_alert_for_elderly(
            db=db,
            elderly_id=profile.id,
            alert_type="AI_RISK",
            title=f"AI xavf: {final_risk}",
            message=summary,
            severity=severity,
        )

    return assessment


@router.get("/ai-assessments", response_model=List[AiAssessmentResponse])
def get_my_ai_assessments(
    limit: int = 10,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_profile(current_user, db)
    return (
        db.query(AiAssessment)
        .filter(AiAssessment.elderly_id == profile.id)
        .order_by(AiAssessment.created_at.desc())
        .limit(limit)
        .all()
    )


# ── Alerts ────────────────────────────────────────────────────────────────────

@router.get("/alerts", response_model=List[AlertResponse])
def get_my_alerts(
    limit: int = 30,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_profile(current_user, db)
    return (
        db.query(Alert)
        .filter(Alert.elderly_id == profile.id)
        .order_by(Alert.created_at.desc())
        .limit(limit)
        .all()
    )


# ── SOS ───────────────────────────────────────────────────────────────────────

class SosRequest(BaseModel):
    message: Optional[str] = "Menga yordam kerak!"
    location: Optional[str] = None


class SosResponse(BaseModel):
    alert_id: str
    message: str
    emergency_phone: Optional[str] = None


@router.post("/sos", response_model=SosResponse)
def send_sos(
    body: SosRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_profile(current_user, db)

    sos_message = body.message or "Menga yordam kerak!"
    if body.location:
        sos_message += f" Joylashuv: {body.location}"

    alert = create_alert_for_elderly(
        db=db,
        elderly_id=profile.id,
        alert_type="SOS",
        title=f"🆘 SOS: {profile.full_name}",
        message=sos_message,
        severity="EMERGENCY",
    )

    return SosResponse(
        alert_id=alert.id if alert else "unknown",
        message="SOS xabari farzandingizga yuborildi. 103 ga qo'ng'iroq qiling!",
        emergency_phone=profile.emergency_phone,
    )


# ── Devices ───────────────────────────────────────────────────────────────────

class DeviceInfo(BaseModel):
    type: str
    name: str
    connected: bool
    last_sync: Optional[str] = None
    battery: Optional[int] = None


class DevicesResponse(BaseModel):
    devices: List[DeviceInfo]


@router.get("/devices", response_model=DevicesResponse)
def get_my_devices(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_profile(current_user, db)

    latest_watch = (
        db.query(WatchData)
        .filter(WatchData.elderly_id == profile.id)
        .order_by(WatchData.recorded_at.desc())
        .first()
    )

    devices = []
    if latest_watch:
        devices.append(DeviceInfo(
            type="smartwatch",
            name="Smart soat",
            connected=True,
            last_sync=latest_watch.recorded_at.strftime("%Y-%m-%dT%H:%M:%S"),
            battery=latest_watch.battery,
        ))
    else:
        devices.append(DeviceInfo(
            type="smartwatch",
            name="Smart soat",
            connected=False,
        ))

    return DevicesResponse(devices=devices)


# ── Doctor Summary ────────────────────────────────────────────────────────────

class DoctorSummaryResponse(BaseModel):
    elderly_name: str
    age: Optional[int]
    chronic_diseases: Optional[str]
    last_health_record: Optional[HealthRecordResponse]
    recent_assessments_count: int
    latest_risk_level: Optional[str]
    active_medications: int
    generated_at: str


@router.get("/doctor-summary", response_model=DoctorSummaryResponse)
def get_doctor_summary(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_my_profile(current_user, db)

    last_health = (
        db.query(HealthRecord)
        .filter(HealthRecord.elderly_id == profile.id)
        .order_by(HealthRecord.created_at.desc())
        .first()
    )

    latest_assessment = (
        db.query(AiAssessment)
        .filter(AiAssessment.elderly_id == profile.id)
        .order_by(AiAssessment.created_at.desc())
        .first()
    )

    thirty_days_ago = datetime.now(timezone.utc) - timedelta(days=30)
    recent_count = (
        db.query(AiAssessment)
        .filter(
            AiAssessment.elderly_id == profile.id,
            AiAssessment.created_at >= thirty_days_ago,
        )
        .count()
    )

    active_meds = (
        db.query(Medication)
        .filter(Medication.elderly_id == profile.id, Medication.active == True)
        .count()
    )

    return DoctorSummaryResponse(
        elderly_name=profile.full_name,
        age=profile.age,
        chronic_diseases=profile.chronic_diseases,
        last_health_record=HealthRecordResponse.model_validate(last_health) if last_health else None,
        recent_assessments_count=recent_count,
        latest_risk_level=latest_assessment.risk_level if latest_assessment else None,
        active_medications=active_meds,
        generated_at=datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%S"),
    )
