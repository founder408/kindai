from pydantic import BaseModel
from typing import Optional
from datetime import datetime


class HealthRecordCreate(BaseModel):
    systolic_bp: Optional[int] = None
    diastolic_bp: Optional[int] = None
    blood_sugar: Optional[float] = None
    heart_rate: Optional[int] = None
    spo2: Optional[int] = None
    temperature: Optional[float] = None
    symptoms: Optional[str] = None
    mood: Optional[str] = None


class HealthRecordResponse(BaseModel):
    id: str
    elderly_id: str
    systolic_bp: Optional[int] = None
    diastolic_bp: Optional[int] = None
    blood_sugar: Optional[float] = None
    heart_rate: Optional[int] = None
    spo2: Optional[int] = None
    temperature: Optional[float] = None
    symptoms: Optional[str] = None
    mood: Optional[str] = None
    created_at: datetime

    model_config = {"from_attributes": True}
