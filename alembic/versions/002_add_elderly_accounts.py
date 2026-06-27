"""Add elderly account support fields

Revision ID: 002
Revises: 001
Create Date: 2024-01-02 00:00:00.000000

"""
from typing import Sequence, Union
from alembic import op
import sqlalchemy as sa

revision: str = "002"
down_revision: Union[str, None] = "001"
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.add_column("users", sa.Column("must_change_password", sa.Boolean, server_default=sa.text("false"), nullable=False))
    op.add_column("users", sa.Column("is_active", sa.Boolean, server_default=sa.text("true"), nullable=False))

    op.add_column("elderly_profiles", sa.Column("elderly_user_id", sa.String(36), sa.ForeignKey("users.id"), nullable=True))
    op.add_column("elderly_profiles", sa.Column("relationship_to_elderly", sa.String(50), nullable=True))
    op.add_column("elderly_profiles", sa.Column("consent_confirmed", sa.Boolean, server_default=sa.text("false"), nullable=False))
    op.add_column("elderly_profiles", sa.Column("consent_confirmed_at", sa.DateTime(timezone=True), nullable=True))

    op.add_column("ai_assessments", sa.Column("caregiver_summary", sa.Text, nullable=True))
    op.add_column("ai_assessments", sa.Column("elderly_summary", sa.Text, nullable=True))


def downgrade() -> None:
    op.drop_column("ai_assessments", "elderly_summary")
    op.drop_column("ai_assessments", "caregiver_summary")
    op.drop_column("elderly_profiles", "consent_confirmed_at")
    op.drop_column("elderly_profiles", "consent_confirmed")
    op.drop_column("elderly_profiles", "relationship_to_elderly")
    op.drop_column("elderly_profiles", "elderly_user_id")
    op.drop_column("users", "is_active")
    op.drop_column("users", "must_change_password")
