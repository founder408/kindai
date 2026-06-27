from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app.core.database import get_db
from app.core.security import get_current_user
from app.models.user import User
from app.models.elderly import ElderlyProfile
from app.models.health_record import HealthRecord
from app.schemas.health_record import HealthRecordCreate, HealthRecordResponse

router = APIRouter(prefix="/api/elderly/{elderly_id}/health-records", tags=["Health Records"])


def verify_elderly_access(elderly_id: str, user: User, db: Session) -> ElderlyProfile:
    profile = db.query(ElderlyProfile).filter(ElderlyProfile.id == elderly_id).first()
    if not profile:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Elderly profile not found")
    if profile.child_user_id != user.id:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Access denied")
    return profile


@router.post("", response_model=HealthRecordResponse, status_code=status.HTTP_201_CREATED)
def create_health_record(
    elderly_id: str,
    data: HealthRecordCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    verify_elderly_access(elderly_id, current_user, db)
    record = HealthRecord(elderly_id=elderly_id, **data.model_dump())
    db.add(record)
    db.commit()
    db.refresh(record)
    return record


@router.get("", response_model=List[HealthRecordResponse])
def list_health_records(
    elderly_id: str,
    limit: int = 20,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    verify_elderly_access(elderly_id, current_user, db)
    return (
        db.query(HealthRecord)
        .filter(HealthRecord.elderly_id == elderly_id)
        .order_by(HealthRecord.created_at.desc())
        .limit(limit)
        .all()
    )


@router.get("/latest", response_model=HealthRecordResponse)
def get_latest_health_record(
    elderly_id: str,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    verify_elderly_access(elderly_id, current_user, db)
    record = (
        db.query(HealthRecord)
        .filter(HealthRecord.elderly_id == elderly_id)
        .order_by(HealthRecord.created_at.desc())
        .first()
    )
    if not record:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="No health records found")
    return record
