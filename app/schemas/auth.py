from pydantic import BaseModel, EmailStr
from typing import Optional


class RegisterRequest(BaseModel):
    full_name: str
    phone: Optional[str] = None
    email: Optional[EmailStr] = None
    password: str
    role: str = "CHILD"


class LoginRequest(BaseModel):
    login: str
    password: str


class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    must_change_password: bool = False
    role: str = "CHILD"


class ChangePasswordRequest(BaseModel):
    current_password: str
    new_password: str
