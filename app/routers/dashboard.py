from datetime import datetime, timezone, timedelta
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.core.security import get_current_user
from app.models.user import User
from app.models.elderly import ElderlyProfile
from app.models.health_record import HealthRecord
from app.models.watch_data import WatchData
from app.models.ai_assessment import AiAssessment
from app.models.alert import Alert
from app.models.medication import Medication, MedicationLog
from app.schemas.dashboard import DashboardResponse, MedicationAdherence
from app.schemas.elderly import ElderlyResponse
from app.schemas.health_record import HealthRecordResponse
from app.schemas.watch_data import WatchDataResponse
from app.schemas.ai_assessment import AiAssessmentResponse

router = APIRouter(prefix="/api/elderly/{elderly_id}/dashboard", tags=["Dashboard"])


@router.get("", response_model=DashboardResponse)
def get_dashboard(
    elderly_id: str,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = db.query(ElderlyProfile).filter(ElderlyProfile.id == elderly_id).first()
    if not profile:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Elderly profile not found")
    if profile.child_user_id != current_user.id:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Access denied")

    last_health = (
        db.query(HealthRecord)
        .filter(HealthRecord.elderly_id == elderly_id)
        .order_by(HealthRecord.created_at.desc())
        .first()
    )

    last_watch = (
        db.query(WatchData)
        .filter(WatchData.elderly_id == elderly_id)
        .order_by(WatchData.recorded_at.desc())
        .first()
    )

    latest_assessment = (
        db.query(AiAssessment)
        .filter(AiAssessment.elderly_id == elderly_id)
        .order_by(AiAssessment.created_at.desc())
        .first()
    )

    unread_count = (
        db.query(Alert)
        .filter(Alert.elderly_id == elderly_id, Alert.is_read == False)
        .count()
    )

    seven_days_ago = datetime.now(timezone.utc) - timedelta(days=7)
    total_scheduled = (
        db.query(MedicationLog)
        .filter(
            MedicationLog.elderly_id == elderly_id,
            MedicationLog.created_at >= seven_days_ago,
        )
        .count()
    )
    total_taken = (
        db.query(MedicationLog)
        .filter(
            MedicationLog.elderly_id == elderly_id,
            MedicationLog.taken == True,
            MedicationLog.created_at >= seven_days_ago,
        )
        .count()
    )
    adherence_percent = (total_taken / total_scheduled * 100) if total_scheduled > 0 else 100.0

    return DashboardResponse(
        elderly=ElderlyResponse.model_validate(profile),
        last_health_record=HealthRecordResponse.model_validate(last_health) if last_health else None,
        last_watch_data=WatchDataResponse.model_validate(last_watch) if last_watch else None,
        latest_ai_assessment=AiAssessmentResponse.model_validate(latest_assessment) if latest_assessment else None,
        unread_alerts_count=unread_count,
        medication_adherence=MedicationAdherence(
            total_scheduled=total_scheduled,
            total_taken=total_taken,
            adherence_percent=round(adherence_percent, 1),
        ),
    )
