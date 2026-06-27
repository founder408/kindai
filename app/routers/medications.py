from datetime import datetime, timezone
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app.core.database import get_db
from app.core.security import get_current_user
from app.models.user import User
from app.models.elderly import ElderlyProfile
from app.models.medication import Medication, MedicationLog
from app.schemas.medication import (
    MedicationCreate,
    MedicationUpdate,
    MedicationResponse,
    MedicationTakenRequest,
    MedicationLogResponse,
)

router = APIRouter(tags=["Medications"])


def verify_elderly_access(elderly_id: str, user: User, db: Session) -> ElderlyProfile:
    profile = db.query(ElderlyProfile).filter(ElderlyProfile.id == elderly_id).first()
    if not profile:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Elderly profile not found")
    if profile.child_user_id != user.id:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Access denied")
    return profile


@router.post(
    "/api/elderly/{elderly_id}/medications",
    response_model=MedicationResponse,
    status_code=status.HTTP_201_CREATED,
)
def create_medication(
    elderly_id: str,
    data: MedicationCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    verify_elderly_access(elderly_id, current_user, db)
    med = Medication(
        elderly_id=elderly_id,
        name=data.name,
        dosage=data.dosage,
        instruction=data.instruction,
        time_to_take=data.time_to_take,
        before_or_after_meal=data.before_or_after_meal,
    )
    db.add(med)
    db.commit()
    db.refresh(med)
    return med


@router.get("/api/elderly/{elderly_id}/medications", response_model=List[MedicationResponse])
def list_medications(
    elderly_id: str,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    verify_elderly_access(elderly_id, current_user, db)
    return (
        db.query(Medication)
        .filter(Medication.elderly_id == elderly_id, Medication.active == True)
        .order_by(Medication.time_to_take)
        .all()
    )


@router.put("/api/medications/{medication_id}", response_model=MedicationResponse)
def update_medication(
    medication_id: str,
    data: MedicationUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    med = db.query(Medication).filter(Medication.id == medication_id).first()
    if not med:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Medication not found")
    verify_elderly_access(med.elderly_id, current_user, db)

    update_data = data.model_dump(exclude_unset=True)
    for key, value in update_data.items():
        setattr(med, key, value)
    db.commit()
    db.refresh(med)
    return med


@router.delete("/api/medications/{medication_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_medication(
    medication_id: str,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    med = db.query(Medication).filter(Medication.id == medication_id).first()
    if not med:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Medication not found")
    verify_elderly_access(med.elderly_id, current_user, db)
    db.delete(med)
    db.commit()


@router.post("/api/medications/{medication_id}/taken", response_model=MedicationLogResponse)
def mark_medication_taken(
    medication_id: str,
    data: MedicationTakenRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    med = db.query(Medication).filter(Medication.id == medication_id).first()
    if not med:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Medication not found")
    verify_elderly_access(med.elderly_id, current_user, db)

    log = MedicationLog(
        medication_id=medication_id,
        elderly_id=med.elderly_id,
        scheduled_time=data.scheduled_time,
        taken=True,
        taken_at=datetime.now(timezone.utc),
    )
    db.add(log)
    db.commit()
    db.refresh(log)
    return log
