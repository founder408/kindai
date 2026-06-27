from pydantic import BaseModel
from typing import Optional
from datetime import datetime


class WatchDataCreate(BaseModel):
    heart_rate: Optional[int] = None
    steps: Optional[int] = None
    sleep_hours: Optional[float] = None
    spo2: Optional[int] = None
    fall_detected: bool = False
    battery: Optional[int] = None


class WatchDataResponse(BaseModel):
    id: str
    elderly_id: str
    heart_rate: Optional[int] = None
    steps: Optional[int] = None
    sleep_hours: Optional[float] = None
    spo2: Optional[int] = None
    fall_detected: bool
    battery: Optional[int] = None
    recorded_at: datetime

    model_config = {"from_attributes": True}
