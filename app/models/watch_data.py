import uuid
from datetime import datetime, timezone
from sqlalchemy import String, Integer, Numeric, Boolean, DateTime, ForeignKey
from sqlalchemy.orm import Mapped, mapped_column
from app.core.database import Base


class WatchData(Base):
    __tablename__ = "watch_data"

    id: Mapped[str] = mapped_column(
        String(36), primary_key=True, default=lambda: str(uuid.uuid4())
    )
    elderly_id: Mapped[str] = mapped_column(
        String(36), ForeignKey("elderly_profiles.id"), nullable=False
    )
    heart_rate: Mapped[int | None] = mapped_column(Integer, nullable=True)
    steps: Mapped[int | None] = mapped_column(Integer, nullable=True)
    sleep_hours: Mapped[float | None] = mapped_column(Numeric(4, 1), nullable=True)
    spo2: Mapped[int | None] = mapped_column(Integer, nullable=True)
    fall_detected: Mapped[bool] = mapped_column(Boolean, default=False)
    battery: Mapped[int | None] = mapped_column(Integer, nullable=True)
    recorded_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=lambda: datetime.now(timezone.utc)
    )
