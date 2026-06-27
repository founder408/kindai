# Import all models so Alembic autogenerate can detect them
from app.models.user import User
from app.models.elderly import ElderlyProfile
from app.models.health_record import HealthRecord
from app.models.medication import Medication, MedicationLog
from app.models.ai_assessment import AiAssessment
from app.models.alert import Alert
from app.models.watch_data import WatchData

# Diabetes models
from app.models.diabetes_profile import DiabetesProfile
from app.models.diabetes_record import DiabetesRecord
from app.models.diabetes_medication import DiabetesMedication, DiabetesMedicationLog
from app.models.diabetes_ai_assessment import DiabetesAiAssessment
from app.models.diabetes_alert import DiabetesAlert
