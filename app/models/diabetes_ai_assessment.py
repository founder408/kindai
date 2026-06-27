import uuid
from datetime import datetime, timezone
from sqlalchemy import String, Text, DateTime, ForeignKey
from sqlalchemy.orm import Mapped, mapped_column
from app.core.database import Base


class DiabetesAiAssessment(Base):
    __tablename__ = "diabetes_ai_assessments"

    id: Mapped[str] = mapped_column(
        String(36), primary_key=True, default=lambda: str(uuid.uuid4())
    )
    profile_id: Mapped[str] = mapped_column(
        String(36), ForeignKey("diabetes_profiles.id"), nullable=False
    )
    user_id: Mapped[str] = mapped_column(
        String(36), ForeignKey("users.id"), nullable=False
    )

    # azure_openai | rule_based
    source: Mapped[str | None] = mapped_column(String(50), nullable=True)
    risk_level: Mapped[str] = mapped_column(String(20))  # LOW|MEDIUM|HIGH|EMERGENCY

    summary: Mapped[str] = mapped_column(Text)  # Simple Uzbek summary
    # JSON array stored as text: ["concern1", "concern2"]
    key_concerns: Mapped[str | None] = mapped_column(Text, nullable=True)
    recommended_next_steps: Mapped[str | None] = mapped_column(Text, nullable=True)
    what_to_monitor: Mapped[str | None] = mapped_column(Text, nullable=True)
    doctor_note: Mapped[str | None] = mapped_column(Text, nullable=True)
    disclaimer: Mapped[str] = mapped_column(
        Text,
        default="Bu tibbiy tashxis emas. Zarur holatda shifokorga murojaat qiling."
    )
    raw_ai_response: Mapped[str | None] = mapped_column(Text, nullable=True)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=lambda: datetime.now(timezone.utc)
    )
