import uuid
from datetime import datetime, time, timezone
from sqlalchemy import String, Numeric, Text, Boolean, Time, DateTime, ForeignKey
from sqlalchemy.orm import Mapped, mapped_column
from app.core.database import Base


class DiabetesMedication(Base):
    """
    Diabetes-specific medication / insulin record.
    Separate from legacy Medication model to avoid breaking existing flow.
    """
    __tablename__ = "diabetes_medications"

    id: Mapped[str] = mapped_column(
        String(36), primary_key=True, default=lambda: str(uuid.uuid4())
    )
    profile_id: Mapped[str] = mapped_column(
        String(36), ForeignKey("diabetes_profiles.id"), nullable=False
    )
    created_by_user_id: Mapped[str] = mapped_column(
        String(36), ForeignKey("users.id"), nullable=False
    )

    name: Mapped[str] = mapped_column(String(255))
    # TABLET | INSULIN | OTHER
    medication_type: Mapped[str] = mapped_column(String(20), default="TABLET")
    dosage: Mapped[str | None] = mapped_column(String(100), nullable=True)
    insulin_units: Mapped[float | None] = mapped_column(Numeric(6, 2), nullable=True)

    # Schedule
    schedule_time: Mapped[time] = mapped_column(Time)
    # DAILY | TWICE_DAILY | THREE_TIMES | WEEKLY | AS_NEEDED
    frequency: Mapped[str] = mapped_column(String(30), default="DAILY")
    before_or_after_meal: Mapped[str | None] = mapped_column(String(20), nullable=True)
    instructions: Mapped[str | None] = mapped_column(Text, nullable=True)

    is_active: Mapped[bool] = mapped_column(Boolean, default=True)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=lambda: datetime.now(timezone.utc)
    )


class DiabetesMedicationLog(Base):
    __tablename__ = "diabetes_medication_logs"

    id: Mapped[str] = mapped_column(
        String(36), primary_key=True, default=lambda: str(uuid.uuid4())
    )
    medication_id: Mapped[str] = mapped_column(
        String(36), ForeignKey("diabetes_medications.id"), nullable=False
    )
    profile_id: Mapped[str] = mapped_column(
        String(36), ForeignKey("diabetes_profiles.id"), nullable=False
    )
    taken_by_user_id: Mapped[str] = mapped_column(
        String(36), ForeignKey("users.id"), nullable=False
    )
    scheduled_time: Mapped[datetime] = mapped_column(DateTime(timezone=True))
    # TAKEN | MISSED | SKIPPED
    status: Mapped[str] = mapped_column(String(10), default="TAKEN")
    taken_at: Mapped[datetime | None] = mapped_column(
        DateTime(timezone=True), nullable=True
    )
    note: Mapped[str | None] = mapped_column(Text, nullable=True)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=lambda: datetime.now(timezone.utc)
    )
