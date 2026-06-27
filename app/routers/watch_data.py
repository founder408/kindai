from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app.core.database import get_db
from app.core.security import get_current_user
from app.models.user import User
from app.models.elderly import ElderlyProfile
from app.models.watch_data import WatchData
from app.schemas.watch_data import WatchDataCreate, WatchDataResponse
from app.services.alert_service import create_alert_for_elderly
from app.services.risk_service import check_watch_battery_alert

router = APIRouter(prefix="/api/elderly/{elderly_id}/watch-data", tags=["Watch Data"])


def verify_elderly_access(elderly_id: str, user: User, db: Session) -> ElderlyProfile:
    profile = db.query(ElderlyProfile).filter(ElderlyProfile.id == elderly_id).first()
    if not profile:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Elderly profile not found")
    if profile.child_user_id != user.id:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Access denied")
    return profile


@router.post("", response_model=WatchDataResponse, status_code=status.HTTP_201_CREATED)
def create_watch_data(
    elderly_id: str,
    data: WatchDataCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    verify_elderly_access(elderly_id, current_user, db)
    watch = WatchData(elderly_id=elderly_id, **data.model_dump())
    db.add(watch)
    db.commit()
    db.refresh(watch)

    battery_alert = check_watch_battery_alert(watch)
    if battery_alert:
        create_alert_for_elderly(
            db=db,
            elderly_id=elderly_id,
            alert_type=battery_alert["type"],
            title=battery_alert["title"],
            message=battery_alert["message"],
            severity=battery_alert["severity"],
        )

    return watch


@router.get("", response_model=List[WatchDataResponse])
def list_watch_data(
    elderly_id: str,
    limit: int = 20,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    verify_elderly_access(elderly_id, current_user, db)
    return (
        db.query(WatchData)
        .filter(WatchData.elderly_id == elderly_id)
        .order_by(WatchData.recorded_at.desc())
        .limit(limit)
        .all()
    )


@router.get("/latest", response_model=WatchDataResponse)
def get_latest_watch_data(
    elderly_id: str,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    verify_elderly_access(elderly_id, current_user, db)
    watch = (
        db.query(WatchData)
        .filter(WatchData.elderly_id == elderly_id)
        .order_by(WatchData.recorded_at.desc())
        .first()
    )
    if not watch:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="No watch data found")
    return watch
