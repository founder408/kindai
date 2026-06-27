"""Initial schema

Revision ID: 001
Revises:
Create Date: 2024-01-01 00:00:00.000000

"""
from typing import Sequence, Union
from alembic import op
import sqlalchemy as sa

revision: str = "001"
down_revision: Union[str, None] = None
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.create_table(
        "users",
        sa.Column("id", sa.String(36), primary_key=True),
        sa.Column("full_name", sa.String(255), nullable=False),
        sa.Column("phone", sa.String(20), unique=True, nullable=True),
        sa.Column("email", sa.String(255), unique=True, nullable=True),
        sa.Column("password_hash", sa.String(255), nullable=False),
        sa.Column("role", sa.String(20), nullable=False, server_default="CHILD"),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now()),
    )

    op.create_table(
        "elderly_profiles",
        sa.Column("id", sa.String(36), primary_key=True),
        sa.Column("child_user_id", sa.String(36), sa.ForeignKey("users.id"), nullable=False),
        sa.Column("full_name", sa.String(255), nullable=False),
        sa.Column("age", sa.Integer, nullable=True),
        sa.Column("gender", sa.String(20), nullable=True),
        sa.Column("chronic_diseases", sa.Text, nullable=True),
        sa.Column("emergency_phone", sa.String(20), nullable=True),
        sa.Column("address", sa.Text, nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now()),
    )

    op.create_table(
        "medications",
        sa.Column("id", sa.String(36), primary_key=True),
        sa.Column("elderly_id", sa.String(36), sa.ForeignKey("elderly_profiles.id"), nullable=False),
        sa.Column("name", sa.String(255), nullable=False),
        sa.Column("dosage", sa.String(100), nullable=True),
        sa.Column("instruction", sa.Text, nullable=True),
        sa.Column("time_to_take", sa.Time, nullable=False),
        sa.Column("before_or_after_meal", sa.String(20), nullable=True),
        sa.Column("active", sa.Boolean, server_default=sa.text("true")),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now()),
    )

    op.create_table(
        "medication_logs",
        sa.Column("id", sa.String(36), primary_key=True),
        sa.Column("medication_id", sa.String(36), sa.ForeignKey("medications.id"), nullable=False),
        sa.Column("elderly_id", sa.String(36), sa.ForeignKey("elderly_profiles.id"), nullable=False),
        sa.Column("scheduled_time", sa.DateTime(timezone=True), nullable=False),
        sa.Column("taken", sa.Boolean, server_default=sa.text("false")),
        sa.Column("taken_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now()),
    )

    op.create_table(
        "health_records",
        sa.Column("id", sa.String(36), primary_key=True),
        sa.Column("elderly_id", sa.String(36), sa.ForeignKey("elderly_profiles.id"), nullable=False),
        sa.Column("systolic_bp", sa.Integer, nullable=True),
        sa.Column("diastolic_bp", sa.Integer, nullable=True),
        sa.Column("blood_sugar", sa.Numeric(6, 2), nullable=True),
        sa.Column("heart_rate", sa.Integer, nullable=True),
        sa.Column("spo2", sa.Integer, nullable=True),
        sa.Column("temperature", sa.Numeric(4, 1), nullable=True),
        sa.Column("symptoms", sa.Text, nullable=True),
        sa.Column("mood", sa.String(50), nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now()),
    )

    op.create_table(
        "watch_data",
        sa.Column("id", sa.String(36), primary_key=True),
        sa.Column("elderly_id", sa.String(36), sa.ForeignKey("elderly_profiles.id"), nullable=False),
        sa.Column("heart_rate", sa.Integer, nullable=True),
        sa.Column("steps", sa.Integer, nullable=True),
        sa.Column("sleep_hours", sa.Numeric(4, 1), nullable=True),
        sa.Column("spo2", sa.Integer, nullable=True),
        sa.Column("fall_detected", sa.Boolean, server_default=sa.text("false")),
        sa.Column("battery", sa.Integer, nullable=True),
        sa.Column("recorded_at", sa.DateTime(timezone=True), server_default=sa.func.now()),
    )

    op.create_table(
        "ai_assessments",
        sa.Column("id", sa.String(36), primary_key=True),
        sa.Column("elderly_id", sa.String(36), sa.ForeignKey("elderly_profiles.id"), nullable=False),
        sa.Column("source", sa.String(50), nullable=True),
        sa.Column("risk_level", sa.String(20), nullable=False),
        sa.Column("summary", sa.Text, nullable=False),
        sa.Column("recommendation", sa.Text, nullable=False),
        sa.Column("raw_ai_response", sa.Text, nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now()),
    )

    op.create_table(
        "alerts",
        sa.Column("id", sa.String(36), primary_key=True),
        sa.Column("elderly_id", sa.String(36), sa.ForeignKey("elderly_profiles.id"), nullable=False),
        sa.Column("child_user_id", sa.String(36), sa.ForeignKey("users.id"), nullable=False),
        sa.Column("type", sa.String(30), nullable=False),
        sa.Column("title", sa.String(255), nullable=False),
        sa.Column("message", sa.Text, nullable=False),
        sa.Column("severity", sa.String(20), nullable=False),
        sa.Column("is_read", sa.Boolean, server_default=sa.text("false")),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now()),
    )


def downgrade() -> None:
    op.drop_table("alerts")
    op.drop_table("ai_assessments")
    op.drop_table("watch_data")
    op.drop_table("health_records")
    op.drop_table("medication_logs")
    op.drop_table("medications")
    op.drop_table("elderly_profiles")
    op.drop_table("users")
