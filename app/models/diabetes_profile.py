import uuid
from datetime import datetime, timezone
from sqlalchemy import String, Integer, Boolean, Text, DateTime, ForeignKey
from sqlalchemy.orm import Mapped, mapped_column
from app.core.database import Base


class DiabetesProfile(Base):
    __tablename__ = "diabetes_profiles"

    id: Mapped[str] = mapped_column(
        String(36), primary_key=True, default=lambda: str(uuid.uuid4())
    )
    # Owner of this diabetes profile (the diabetic user themselves)
    user_id: Mapped[str] = mapped_column(
        String(36), ForeignKey("users.id"), nullable=False, unique=True
    )
    full_name: Mapped[str] = mapped_column(String(255))
    age: Mapped[int | None] = mapped_column(Integer, nullable=True)
    gender: Mapped[str | None] = mapped_column(String(20), nullable=True)

    # Diabetes-specific fields
    diabetes_type: Mapped[str | None] = mapped_column(
        String(50), nullable=True
    )  # TYPE_1, TYPE_2, GESTATIONAL, PREDIABETES, UNKNOWN
    diagnosis_year: Mapped[int | None] = mapped_column(Integer, nullable=True)
    target_glucose_min: Mapped[float | None] = mapped_column(
        String(10), nullable=True
    )  # stored as string e.g. "4.0"
    target_glucose_max: Mapped[float | None] = mapped_column(
        String(10), nullable=True
    )  # stored as string e.g. "10.0"
    glucose_unit: Mapped[str] = mapped_column(String(10), default="mmol/L")

    chronic_diseases: Mapped[str | None] = mapped_column(Text, nullable=True)
    emergency_phone: Mapped[str | None] = mapped_column(String(20), nullable=True)

    # Caregiver link: family member who monitors this user
    caregiver_user_id: Mapped[str | None] = mapped_column(
        String(36), ForeignKey("users.id"), nullable=True
    )
    consent_confirmed: Mapped[bool] = mapped_column(Boolean, default=False)
    consent_confirmed_at: Mapped[datetime | None] = mapped_column(
        DateTime(timezone=True), nullable=True
    )
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=lambda: datetime.now(timezone.utc)
    )
