import uuid
from datetime import datetime, time, timezone
from sqlalchemy import String, Text, Time, Boolean, DateTime, ForeignKey
from sqlalchemy.orm import Mapped, mapped_column
from app.core.database import Base


class Medication(Base):
    __tablename__ = "medications"

    id: Mapped[str] = mapped_column(
        String(36), primary_key=True, default=lambda: str(uuid.uuid4())
    )
    elderly_id: Mapped[str] = mapped_column(
        String(36), ForeignKey("elderly_profiles.id"), nullable=False
    )
    name: Mapped[str] = mapped_column(String(255))
    dosage: Mapped[str | None] = mapped_column(String(100), nullable=True)
    instruction: Mapped[str | None] = mapped_column(Text, nullable=True)
    time_to_take: Mapped[time] = mapped_column(Time)
    before_or_after_meal: Mapped[str | None] = mapped_column(
        String(20), nullable=True
    )
    active: Mapped[bool] = mapped_column(Boolean, default=True)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=lambda: datetime.now(timezone.utc)
    )


class MedicationLog(Base):
    __tablename__ = "medication_logs"

    id: Mapped[str] = mapped_column(
        String(36), primary_key=True, default=lambda: str(uuid.uuid4())
    )
    medication_id: Mapped[str] = mapped_column(
        String(36), ForeignKey("medications.id"), nullable=False
    )
    elderly_id: Mapped[str] = mapped_column(
        String(36), ForeignKey("elderly_profiles.id"), nullable=False
    )
    scheduled_time: Mapped[datetime] = mapped_column(DateTime(timezone=True))
    taken: Mapped[bool] = mapped_column(Boolean, default=False)
    taken_at: Mapped[datetime | None] = mapped_column(
        DateTime(timezone=True), nullable=True
    )
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=lambda: datetime.now(timezone.utc)
    )
