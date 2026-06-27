from sqlalchemy.orm import Session
from app.models.alert import Alert
from app.models.elderly import ElderlyProfile


def create_alert(
    db: Session,
    elderly_id: str,
    child_user_id: str,
    alert_type: str,
    title: str,
    message: str,
    severity: str,
) -> Alert:
    alert = Alert(
        elderly_id=elderly_id,
        child_user_id=child_user_id,
        type=alert_type,
        title=title,
        message=message,
        severity=severity,
    )
    db.add(alert)
    db.commit()
    db.refresh(alert)
    return alert


def create_alert_for_elderly(
    db: Session,
    elderly_id: str,
    alert_type: str,
    title: str,
    message: str,
    severity: str,
) -> Alert:
    profile = (
        db.query(ElderlyProfile)
        .filter(ElderlyProfile.id == elderly_id)
        .first()
    )
    if not profile:
        return None
    return create_alert(
        db=db,
        elderly_id=elderly_id,
        child_user_id=profile.child_user_id,
        alert_type=alert_type,
        title=title,
        message=message,
        severity=severity,
    )
