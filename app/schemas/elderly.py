from pydantic import BaseModel
from typing import Optional
from datetime import datetime


class ElderlyCreate(BaseModel):
    full_name: str
    age: Optional[int] = None
    gender: Optional[str] = None
    chronic_diseases: Optional[str] = None
    emergency_phone: Optional[str] = None
    address: Optional[str] = None
    relationship_to_elderly: Optional[str] = None


class ElderlyCreateWithAccount(BaseModel):
    full_name: str
    age: Optional[int] = None
    gender: Optional[str] = None
    chronic_diseases: Optional[str] = None
    emergency_phone: Optional[str] = None
    address: Optional[str] = None
    relationship_to_elderly: Optional[str] = None
    login_phone: Optional[str] = None
    login_email: Optional[str] = None
    login_password: str


class ElderlyUpdate(BaseModel):
    full_name: Optional[str] = None
    age: Optional[int] = None
    gender: Optional[str] = None
    chronic_diseases: Optional[str] = None
    emergency_phone: Optional[str] = None
    address: Optional[str] = None
    relationship_to_elderly: Optional[str] = None


class ElderlyResponse(BaseModel):
    id: str
    child_user_id: str
    elderly_user_id: Optional[str] = None
    full_name: str
    age: Optional[int] = None
    gender: Optional[str] = None
    chronic_diseases: Optional[str] = None
    emergency_phone: Optional[str] = None
    address: Optional[str] = None
    relationship_to_elderly: Optional[str] = None
    consent_confirmed: bool = False
    consent_confirmed_at: Optional[datetime] = None
    created_at: datetime

    model_config = {"from_attributes": True}
