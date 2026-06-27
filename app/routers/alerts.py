from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app.core.database import get_db
from app.core.security import get_current_user
from app.models.user import User
from app.models.elderly import ElderlyProfile
from app.models.alert import Alert
from app.schemas.alert import AlertResponse

router = APIRouter(tags=["Alerts"])


@router.get("/api/alerts", response_model=List[AlertResponse])
def list_my_alerts(
    is_read: bool = None,
    limit: int = 50,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    query = db.query(Alert).filter(Alert.child_user_id == current_user.id)
    if is_read is not None:
        query = query.filter(Alert.is_read == is_read)
    return query.order_by(Alert.created_at.desc()).limit(limit).all()


@router.get("/api/elderly/{elderly_id}/alerts", response_model=List[AlertResponse])
def list_elderly_alerts(
    elderly_id: str,
    limit: int = 50,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = db.query(ElderlyProfile).filter(ElderlyProfile.id == elderly_id).first()
    if not profile:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Elderly profile not found")
    if profile.child_user_id != current_user.id:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Access denied")

    return (
        db.query(Alert)
        .filter(Alert.elderly_id == elderly_id)
        .order_by(Alert.created_at.desc())
        .limit(limit)
        .all()
    )


@router.put("/api/alerts/{alert_id}/read", response_model=AlertResponse)
def mark_alert_read(
    alert_id: str,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    alert = db.query(Alert).filter(Alert.id == alert_id).first()
    if not alert:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Alert not found")
    if alert.child_user_id != current_user.id:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Access denied")

    alert.is_read = True
    db.commit()
    db.refresh(alert)
    return alert
