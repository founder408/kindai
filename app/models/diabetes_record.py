import uuid
from datetime import datetime, timezone
from sqlalchemy import String, Numeric, Text, DateTime, ForeignKey
from sqlalchemy.orm import Mapped, mapped_column
from app.core.database import Base


class DiabetesRecord(Base):
    """
    Blood glucose measurement record.
    glucose_value is always stored as mmol/L internally;
    display conversion handled in schema/response layer.
    """
    __tablename__ = "diabetes_records"

    id: Mapped[str] = mapped_column(
        String(36), primary_key=True, default=lambda: str(uuid.uuid4())
    )
    profile_id: Mapped[str] = mapped_column(
        String(36), ForeignKey("diabetes_profiles.id"), nullable=False
    )
    user_id: Mapped[str] = mapped_column(
        String(36), ForeignKey("users.id"), nullable=False
    )

    glucose_value: Mapped[float] = mapped_column(Numeric(6, 2), nullable=False)
    unit: Mapped[str] = mapped_column(String(10), default="mmol/L")

    # FASTING | BEFORE_MEAL | AFTER_MEAL | BEDTIME | RANDOM
    measurement_context: Mapped[str] = mapped_column(String(20), default="RANDOM")

    # When the glucose was actually measured (user-supplied, not server time)
    measured_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False
    )

    # Comma-separated symptom tags e.g. "holsizlik,terlash"
    symptoms: Mapped[str | None] = mapped_column(Text, nullable=True)
    notes: Mapped[str | None] = mapped_column(Text, nullable=True)

    # MANUAL | GLUCOMETER | HEALTH_CONNECT | IMPORT
    source: Mapped[str] = mapped_column(String(20), default="MANUAL")

    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=lambda: datetime.now(timezone.utc)
    )
