import uuid
from datetime import datetime, timezone
from sqlalchemy import String, Text, DateTime, ForeignKey
from sqlalchemy.orm import Mapped, mapped_column
from app.core.database import Base


class AiAssessment(Base):
    __tablename__ = "ai_assessments"

    id: Mapped[str] = mapped_column(
        String(36), primary_key=True, default=lambda: str(uuid.uuid4())
    )
    elderly_id: Mapped[str] = mapped_column(
        String(36), ForeignKey("elderly_profiles.id"), nullable=False
    )
    source: Mapped[str | None] = mapped_column(String(50), nullable=True)
    risk_level: Mapped[str] = mapped_column(String(20))
    summary: Mapped[str] = mapped_column(Text)
    recommendation: Mapped[str] = mapped_column(Text)
    caregiver_summary: Mapped[str | None] = mapped_column(Text, nullable=True)
    elderly_summary: Mapped[str | None] = mapped_column(Text, nullable=True)
    raw_ai_response: Mapped[str | None] = mapped_column(Text, nullable=True)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=lambda: datetime.now(timezone.utc)
    )
