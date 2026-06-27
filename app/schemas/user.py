from pydantic import BaseModel
from typing import Optional
from datetime import datetime


class UserResponse(BaseModel):
    id: str
    full_name: str
    phone: Optional[str] = None
    email: Optional[str] = None
    role: str
    must_change_password: bool = False
    is_active: bool = True
    created_at: datetime

    model_config = {"from_attributes": True}
