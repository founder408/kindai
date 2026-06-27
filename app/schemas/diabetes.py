from pydantic import BaseModel, field_validator
from typing import Optional, List
from datetime import datetime, time


# ── DiabetesProfile ───────────────────────────────────────────────────────────

class DiabetesProfileCreate(BaseModel):
    full_name: str
    age: Optional[int] = None
    gender: Optional[str] = None
    diabetes_type: Optional[str] = None  # TYPE_1|TYPE_2|GESTATIONAL|PREDIABETES|UNKNOWN
    diagnosis_year: Optional[int] = None
    target_glucose_min: Optional[str] = None
    target_glucose_max: Optional[str] = None
    glucose_unit: str = "mmol/L"
    chronic_diseases: Optional[str] = None
    emergency_phone: Optional[str] = None


class DiabetesProfileUpdate(BaseModel):
    full_name: Optional[str] = None
    age: Optional[int] = None
    gender: Optional[str] = None
    diabetes_type: Optional[str] = None
    diagnosis_year: Optional[int] = None
    target_glucose_min: Optional[str] = None
    target_glucose_max: Optional[str] = None
    glucose_unit: Optional[str] = None
    chronic_diseases: Optional[str] = None
    emergency_phone: Optional[str] = None


class DiabetesProfileResponse(BaseModel):
    id: str
    user_id: str
    full_name: str
    age: Optional[int] = None
    gender: Optional[str] = None
    diabetes_type: Optional[str] = None
    diagnosis_year: Optional[int] = None
    target_glucose_min: Optional[str] = None
    target_glucose_max: Optional[str] = None
    glucose_unit: str
    chronic_diseases: Optional[str] = None
    emergency_phone: Optional[str] = None
    caregiver_user_id: Optional[str] = None
    consent_confirmed: bool
    created_at: datetime

    model_config = {"from_attributes": True}


# ── DiabetesRecord ────────────────────────────────────────────────────────────

class DiabetesRecordCreate(BaseModel):
    glucose_value: float
    unit: str = "mmol/L"
    measurement_context: str = "RANDOM"
    measured_at: datetime
    symptoms: Optional[str] = None
    notes: Optional[str] = None
    source: str = "MANUAL"

    @field_validator("measurement_context")
    @classmethod
    def validate_context(cls, v: str) -> str:
        valid = {"FASTING", "BEFORE_MEAL", "AFTER_MEAL", "BEDTIME", "RANDOM"}
        if v.upper() not in valid:
            raise ValueError(f"measurement_context must be one of {valid}")
        return v.upper()

    @field_validator("source")
    @classmethod
    def validate_source(cls, v: str) -> str:
        valid = {"MANUAL", "GLUCOMETER", "HEALTH_CONNECT", "IMPORT"}
        if v.upper() not in valid:
            return "MANUAL"
        return v.upper()

    @field_validator("glucose_value")
    @classmethod
    def validate_glucose(cls, v: float) -> float:
        if v <= 0 or v > 55:
            raise ValueError("Glucose value must be between 0.1 and 55 mmol/L")
        return round(v, 2)


class DiabetesRecordResponse(BaseModel):
    id: str
    profile_id: str
    user_id: str
    glucose_value: float
    unit: str
    measurement_context: str
    measured_at: datetime
    symptoms: Optional[str] = None
    notes: Optional[str] = None
    source: str
    created_at: datetime

    model_config = {"from_attributes": True}


# ── Glucose Trends ────────────────────────────────────────────────────────────

class GlucoseTrendsResponse(BaseModel):
    period: str  # 7d | 30d
    total_records: int
    average_glucose: Optional[float] = None
    highest_glucose: Optional[float] = None
    lowest_glucose: Optional[float] = None
    fasting_average: Optional[float] = None
    after_meal_average: Optional[float] = None
    before_meal_average: Optional[float] = None
    bedtime_average: Optional[float] = None
    # IMPROVED | STABLE | WORSE | NEEDS_ATTENTION
    trend_direction: str = "STABLE"
    # Daily data points: [{"date": "2026-06-01", "avg": 8.2, "count": 3}]
    daily_data: List[dict] = []


# ── DiabetesMedication ────────────────────────────────────────────────────────

class DiabetesMedicationCreate(BaseModel):
    name: str
    medication_type: str = "TABLET"  # TABLET | INSULIN | OTHER
    dosage: Optional[str] = None
    insulin_units: Optional[float] = None
    schedule_time: time
    frequency: str = "DAILY"
    before_or_after_meal: Optional[str] = None
    instructions: Optional[str] = None

    @field_validator("medication_type")
    @classmethod
    def validate_type(cls, v: str) -> str:
        valid = {"TABLET", "INSULIN", "OTHER"}
        if v.upper() not in valid:
            raise ValueError(f"medication_type must be one of {valid}")
        return v.upper()


class DiabetesMedicationUpdate(BaseModel):
    name: Optional[str] = None
    medication_type: Optional[str] = None
    dosage: Optional[str] = None
    insulin_units: Optional[float] = None
    schedule_time: Optional[time] = None
    frequency: Optional[str] = None
    before_or_after_meal: Optional[str] = None
    instructions: Optional[str] = None
    is_active: Optional[bool] = None


class DiabetesMedicationResponse(BaseModel):
    id: str
    profile_id: str
    name: str
    medication_type: str
    dosage: Optional[str] = None
    insulin_units: Optional[float] = None
    schedule_time: time
    frequency: str
    before_or_after_meal: Optional[str] = None
    instructions: Optional[str] = None
    is_active: bool
    created_at: datetime

    model_config = {"from_attributes": True}


class DiabetesMedicationLogResponse(BaseModel):
    id: str
    medication_id: str
    profile_id: str
    scheduled_time: datetime
    status: str
    taken_at: Optional[datetime] = None
    note: Optional[str] = None
    created_at: datetime

    model_config = {"from_attributes": True}


class MedicationTakenRequest(BaseModel):
    scheduled_time: datetime
    status: str = "TAKEN"  # TAKEN | MISSED | SKIPPED
    note: Optional[str] = None


class DiabetesAdherenceResponse(BaseModel):
    period_days: int
    total_scheduled: int
    total_taken: int
    total_missed: int
    total_skipped: int
    adherence_percent: float
    streak_days: int  # consecutive days with full adherence


# ── AI Assessment ─────────────────────────────────────────────────────────────

class DiabetesAiAssessmentResponse(BaseModel):
    id: str
    profile_id: str
    source: Optional[str] = None
    risk_level: str
    summary: str
    key_concerns: Optional[List[str]] = None
    recommended_next_steps: Optional[List[str]] = None
    what_to_monitor: Optional[List[str]] = None
    doctor_note: Optional[str] = None
    disclaimer: str
    created_at: datetime

    model_config = {"from_attributes": True}


# ── Alerts ────────────────────────────────────────────────────────────────────

class DiabetesAlertResponse(BaseModel):
    id: str
    profile_id: str
    type: str
    title: str
    message: str
    severity: str
    is_read: bool
    created_at: datetime

    model_config = {"from_attributes": True}


class SosRequest(BaseModel):
    message: str = "Menga yordam kerak!"
    location: Optional[str] = None


class SosResponse(BaseModel):
    alert_id: str
    message: str
    emergency_phone: Optional[str] = None


# ── Dashboard ─────────────────────────────────────────────────────────────────

class MedicationAdherenceSummary(BaseModel):
    total_scheduled: int
    total_taken: int
    adherence_percent: float


class WeeklySummary(BaseModel):
    average_glucose: Optional[float] = None
    highest_glucose: Optional[float] = None
    lowest_glucose: Optional[float] = None
    trend_direction: str = "STABLE"
    total_records: int = 0


class DiabetesDashboardResponse(BaseModel):
    profile: DiabetesProfileResponse
    today_glucose: Optional[DiabetesRecordResponse] = None
    latest_glucose: Optional[DiabetesRecordResponse] = None
    today_medications: List[DiabetesMedicationResponse] = []
    medication_adherence: Optional[MedicationAdherenceSummary] = None
    latest_ai_assessment: Optional[DiabetesAiAssessmentResponse] = None
    unread_alerts: int = 0
    weekly_summary: Optional[WeeklySummary] = None


# ── Doctor Summary ────────────────────────────────────────────────────────────

class GlucoseSummary(BaseModel):
    period: str
    total_records: int
    average_glucose: Optional[float] = None
    highest_glucose: Optional[float] = None
    lowest_glucose: Optional[float] = None
    fasting_average: Optional[float] = None
    after_meal_average: Optional[float] = None
    in_range_percent: Optional[float] = None  # % readings within target


class DiabetesDoctorSummaryResponse(BaseModel):
    period: str
    profile: DiabetesProfileResponse
    glucose_summary: GlucoseSummary
    medication_adherence: DiabetesAdherenceResponse
    symptoms_summary: Optional[str] = None
    latest_ai_summary: Optional[str] = None
    latest_risk_level: Optional[str] = None
    doctor_note: Optional[str] = None
    disclaimer: str = "Bu tibbiy tashxis emas. Zarur holatda shifokorga murojaat qiling."
