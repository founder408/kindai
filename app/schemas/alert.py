from pydantic import BaseModel
from typing import Optional
from datetime import datetime


class AlertResponse(BaseModel):
    id: str
    elderly_id: str
    child_user_id: str
    type: str
    title: str
    message: str
    severity: str
    is_read: bool
    created_at: datetime

    model_config = {"from_attributes": True}
