from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app.core.database import get_db
from app.core.security import get_current_user, hash_password
from app.models.user import User
from app.models.elderly import ElderlyProfile
from app.schemas.elderly import ElderlyCreate, ElderlyCreateWithAccount, ElderlyUpdate, ElderlyResponse

router = APIRouter(prefix="/api/elderly", tags=["Elderly Profiles"])


def get_elderly_or_404(elderly_id: str, user: User, db: Session) -> ElderlyProfile:
    profile = db.query(ElderlyProfile).filter(ElderlyProfile.id == elderly_id).first()
    if not profile:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Elderly profile not found")
    if profile.child_user_id != user.id:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Access denied")
    return profile


@router.post("", response_model=ElderlyResponse, status_code=status.HTTP_201_CREATED)
def create_elderly(
    data: ElderlyCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = ElderlyProfile(
        child_user_id=current_user.id,
        full_name=data.full_name,
        age=data.age,
        gender=data.gender,
        chronic_diseases=data.chronic_diseases,
        emergency_phone=data.emergency_phone,
        address=data.address,
        relationship_to_elderly=data.relationship_to_elderly,
    )
    db.add(profile)
    db.commit()
    db.refresh(profile)
    return profile


@router.post("/with-account", response_model=ElderlyResponse, status_code=status.HTTP_201_CREATED)
def create_elderly_with_account(
    data: ElderlyCreateWithAccount,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    if not data.login_phone and not data.login_email:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Phone or email is required for elderly account",
        )

    if data.login_phone:
        existing = db.query(User).filter(User.phone == data.login_phone).first()
        if existing:
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail="Phone already registered",
            )

    if data.login_email:
        existing = db.query(User).filter(User.email == data.login_email).first()
        if existing:
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail="Email already registered",
            )

    elderly_user = User(
        full_name=data.full_name,
        phone=data.login_phone,
        email=data.login_email,
        password_hash=hash_password(data.login_password),
        role="ELDERLY",
        must_change_password=True,
    )
    db.add(elderly_user)
    db.flush()

    profile = ElderlyProfile(
        child_user_id=current_user.id,
        elderly_user_id=elderly_user.id,
        full_name=data.full_name,
        age=data.age,
        gender=data.gender,
        chronic_diseases=data.chronic_diseases,
        emergency_phone=data.emergency_phone,
        address=data.address,
        relationship_to_elderly=data.relationship_to_elderly,
    )
    db.add(profile)
    db.commit()
    db.refresh(profile)
    return profile


@router.get("", response_model=List[ElderlyResponse])
def list_elderly(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    return (
        db.query(ElderlyProfile)
        .filter(ElderlyProfile.child_user_id == current_user.id)
        .order_by(ElderlyProfile.created_at.desc())
        .all()
    )


@router.get("/{elderly_id}", response_model=ElderlyResponse)
def get_elderly(
    elderly_id: str,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    return get_elderly_or_404(elderly_id, current_user, db)


@router.put("/{elderly_id}", response_model=ElderlyResponse)
def update_elderly(
    elderly_id: str,
    data: ElderlyUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_elderly_or_404(elderly_id, current_user, db)
    update_data = data.model_dump(exclude_unset=True)
    for key, value in update_data.items():
        setattr(profile, key, value)
    db.commit()
    db.refresh(profile)
    return profile


@router.delete("/{elderly_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_elderly(
    elderly_id: str,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    profile = get_elderly_or_404(elderly_id, current_user, db)
    db.delete(profile)
    db.commit()
