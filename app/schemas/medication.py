from pydantic import BaseModel
from typing import Optional
from datetime import datetime, time


class MedicationCreate(BaseModel):
    name: str
    dosage: Optional[str] = None
    instruction: Optional[str] = None
    time_to_take: time
    before_or_after_meal: Optional[str] = None


class MedicationUpdate(BaseModel):
    name: Optional[str] = None
    dosage: Optional[str] = None
    instruction: Optional[str] = None
    time_to_take: Optional[time] = None
    before_or_after_meal: Optional[str] = None
    active: Optional[bool] = None


class MedicationResponse(BaseModel):
    id: str
    elderly_id: str
    name: str
    dosage: Optional[str] = None
    instruction: Optional[str] = None
    time_to_take: time
    before_or_after_meal: Optional[str] = None
    active: bool
    created_at: datetime

    model_config = {"from_attributes": True}


class MedicationTakenRequest(BaseModel):
    scheduled_time: datetime


class MedicationLogResponse(BaseModel):
    id: str
    medication_id: str
    elderly_id: str
    scheduled_time: datetime
    taken: bool
    taken_at: Optional[datetime] = None
    created_at: datetime

    model_config = {"from_attributes": True}
