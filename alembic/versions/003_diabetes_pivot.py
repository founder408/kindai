"""Diabetes pivot: add diabetes-specific tables (additive only, no drops)

Revision ID: 003
Revises: 002
Create Date: 2026-06-27 00:00:00.000000

Strategy:
  - All changes are purely additive (CREATE TABLE, ADD COLUMN).
  - Existing tables (elderly_profiles, health_records, medications, etc.) are untouched.
  - User.role gets new value 'DIABETIC' — no schema change needed (VARCHAR).
"""
from typing import Sequence, Union
from alembic import op
import sqlalchemy as sa

revision: str = "003"
down_revision: Union[str, None] = "002"
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    # ── diabetes_profiles ────────────────────────────────────────────────────
    op.create_table(
        "diabetes_profiles",
        sa.Column("id", sa.String(36), primary_key=True),
        sa.Column("user_id", sa.String(36), sa.ForeignKey("users.id"), nullable=False, unique=True),
        sa.Column("full_name", sa.String(255), nullable=False),
        sa.Column("age", sa.Integer, nullable=True),
        sa.Column("gender", sa.String(20), nullable=True),
        sa.Column("diabetes_type", sa.String(50), nullable=True),
        sa.Column("diagnosis_year", sa.Integer, nullable=True),
        sa.Column("target_glucose_min", sa.String(10), nullable=True),
        sa.Column("target_glucose_max", sa.String(10), nullable=True),
        sa.Column("glucose_unit", sa.String(10), server_default="mmol/L", nullable=False),
        sa.Column("chronic_diseases", sa.Text, nullable=True),
        sa.Column("emergency_phone", sa.String(20), nullable=True),
        sa.Column("caregiver_user_id", sa.String(36), sa.ForeignKey("users.id"), nullable=True),
        sa.Column("consent_confirmed", sa.Boolean, server_default=sa.text("false"), nullable=False),
        sa.Column("consent_confirmed_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column(
            "created_at",
            sa.DateTime(timezone=True),
            server_default=sa.text("NOW()"),
            nullable=False,
        ),
    )

    # ── diabetes_records ─────────────────────────────────────────────────────
    op.create_table(
        "diabetes_records",
        sa.Column("id", sa.String(36), primary_key=True),
        sa.Column("profile_id", sa.String(36), sa.ForeignKey("diabetes_profiles.id"), nullable=False),
        sa.Column("user_id", sa.String(36), sa.ForeignKey("users.id"), nullable=False),
        sa.Column("glucose_value", sa.Numeric(6, 2), nullable=False),
        sa.Column("unit", sa.String(10), server_default="mmol/L", nullable=False),
        sa.Column("measurement_context", sa.String(20), server_default="RANDOM", nullable=False),
        sa.Column("measured_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("symptoms", sa.Text, nullable=True),
        sa.Column("notes", sa.Text, nullable=True),
        sa.Column("source", sa.String(20), server_default="MANUAL", nullable=False),
        sa.Column(
            "created_at",
            sa.DateTime(timezone=True),
            server_default=sa.text("NOW()"),
            nullable=False,
        ),
    )
    op.create_index("ix_diabetes_records_profile_measured", "diabetes_records", ["profile_id", "measured_at"])

    # ── diabetes_medications ──────────────────────────────────────────────────
    op.create_table(
        "diabetes_medications",
        sa.Column("id", sa.String(36), primary_key=True),
        sa.Column("profile_id", sa.String(36), sa.ForeignKey("diabetes_profiles.id"), nullable=False),
        sa.Column("created_by_user_id", sa.String(36), sa.ForeignKey("users.id"), nullable=False),
        sa.Column("name", sa.String(255), nullable=False),
        sa.Column("medication_type", sa.String(20), server_default="TABLET", nullable=False),
        sa.Column("dosage", sa.String(100), nullable=True),
        sa.Column("insulin_units", sa.Numeric(6, 2), nullable=True),
        sa.Column("schedule_time", sa.Time, nullable=False),
        sa.Column("frequency", sa.String(30), server_default="DAILY", nullable=False),
        sa.Column("before_or_after_meal", sa.String(20), nullable=True),
        sa.Column("instructions", sa.Text, nullable=True),
        sa.Column("is_active", sa.Boolean, server_default=sa.text("true"), nullable=False),
        sa.Column(
            "created_at",
            sa.DateTime(timezone=True),
            server_default=sa.text("NOW()"),
            nullable=False,
        ),
    )

    # ── diabetes_medication_logs ──────────────────────────────────────────────
    op.create_table(
        "diabetes_medication_logs",
        sa.Column("id", sa.String(36), primary_key=True),
        sa.Column("medication_id", sa.String(36), sa.ForeignKey("diabetes_medications.id"), nullable=False),
        sa.Column("profile_id", sa.String(36), sa.ForeignKey("diabetes_profiles.id"), nullable=False),
        sa.Column("taken_by_user_id", sa.String(36), sa.ForeignKey("users.id"), nullable=False),
        sa.Column("scheduled_time", sa.DateTime(timezone=True), nullable=False),
        sa.Column("status", sa.String(10), server_default="TAKEN", nullable=False),
        sa.Column("taken_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("note", sa.Text, nullable=True),
        sa.Column(
            "created_at",
            sa.DateTime(timezone=True),
            server_default=sa.text("NOW()"),
            nullable=False,
        ),
    )

    # ── diabetes_ai_assessments ───────────────────────────────────────────────
    op.create_table(
        "diabetes_ai_assessments",
        sa.Column("id", sa.String(36), primary_key=True),
        sa.Column("profile_id", sa.String(36), sa.ForeignKey("diabetes_profiles.id"), nullable=False),
        sa.Column("user_id", sa.String(36), sa.ForeignKey("users.id"), nullable=False),
        sa.Column("source", sa.String(50), nullable=True),
        sa.Column("risk_level", sa.String(20), nullable=False),
        sa.Column("summary", sa.Text, nullable=False),
        sa.Column("key_concerns", sa.Text, nullable=True),
        sa.Column("recommended_next_steps", sa.Text, nullable=True),
        sa.Column("what_to_monitor", sa.Text, nullable=True),
        sa.Column("doctor_note", sa.Text, nullable=True),
        sa.Column(
            "disclaimer",
            sa.Text,
            server_default="Bu tibbiy tashxis emas. Zarur holatda shifokorga murojaat qiling.",
            nullable=False,
        ),
        sa.Column("raw_ai_response", sa.Text, nullable=True),
        sa.Column(
            "created_at",
            sa.DateTime(timezone=True),
            server_default=sa.text("NOW()"),
            nullable=False,
        ),
    )

    # ── diabetes_alerts ───────────────────────────────────────────────────────
    op.create_table(
        "diabetes_alerts",
        sa.Column("id", sa.String(36), primary_key=True),
        sa.Column("profile_id", sa.String(36), sa.ForeignKey("diabetes_profiles.id"), nullable=False),
        sa.Column("user_id", sa.String(36), sa.ForeignKey("users.id"), nullable=False),
        sa.Column("type", sa.String(30), nullable=False),
        sa.Column("title", sa.String(255), nullable=False),
        sa.Column("message", sa.Text, nullable=False),
        sa.Column("severity", sa.String(20), nullable=False),
        sa.Column("is_read", sa.Boolean, server_default=sa.text("false"), nullable=False),
        sa.Column(
            "created_at",
            sa.DateTime(timezone=True),
            server_default=sa.text("NOW()"),
            nullable=False,
        ),
    )


def downgrade() -> None:
    op.drop_table("diabetes_alerts")
    op.drop_table("diabetes_ai_assessments")
    op.drop_table("diabetes_medication_logs")
    op.drop_table("diabetes_medications")
    op.drop_index("ix_diabetes_records_profile_measured", table_name="diabetes_records")
    op.drop_table("diabetes_records")
    op.drop_table("diabetes_profiles")
