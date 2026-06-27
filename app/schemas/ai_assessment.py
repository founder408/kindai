from pydantic import BaseModel
from typing import Optional
from datetime import datetime


class AiAssessmentResponse(BaseModel):
    id: str
    elderly_id: str
    source: Optional[str] = None
    risk_level: str
    summary: str
    recommendation: str
    caregiver_summary: Optional[str] = None
    elderly_summary: Optional[str] = None
    created_at: datetime

    model_config = {"from_attributes": True}
