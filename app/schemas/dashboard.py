from pydantic import BaseModel
from typing import Optional
from app.schemas.elderly import ElderlyResponse
from app.schemas.health_record import HealthRecordResponse
from app.schemas.watch_data import WatchDataResponse
from app.schemas.ai_assessment import AiAssessmentResponse


class MedicationAdherence(BaseModel):
    total_scheduled: int
    total_taken: int
    adherence_percent: float


class DashboardResponse(BaseModel):
    elderly: ElderlyResponse
    last_health_record: Optional[HealthRecordResponse] = None
    last_watch_data: Optional[WatchDataResponse] = None
    latest_ai_assessment: Optional[AiAssessmentResponse] = None
    unread_alerts_count: int
    medication_adherence: MedicationAdherence
