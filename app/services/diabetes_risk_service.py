"""
Diabetes-specific rule-based risk assessment.
Used as fallback when Azure OpenAI is unavailable.
Never diagnoses — only signals risk level.
"""
from typing import Optional, List
from app.models.diabetes_record import DiabetesRecord
from app.models.diabetes_profile import DiabetesProfile


DISCLAIMER = (
    "Bu tibbiy tashxis emas. Zarur holatda shifokorga murojaat qiling. "
    "Kind AI dori yoki insulin dozasini belgilamaydi."
)

# Glucose thresholds (mmol/L) — used only as cautious risk signals
_GLUCOSE_VERY_HIGH = 16.7   # ~300 mg/dL
_GLUCOSE_HIGH = 11.1         # ~200 mg/dL
_GLUCOSE_MODERATE = 8.0      # above normal post-meal range
_GLUCOSE_LOW = 4.0           # below normal
_GLUCOSE_VERY_LOW = 3.0      # hypoglycaemia signal


def max_risk(current: str, new: str) -> str:
    levels = {"LOW": 0, "MEDIUM": 1, "HIGH": 2, "EMERGENCY": 3}
    return new if levels.get(new, 0) > levels.get(current, 0) else current


def diabetes_rule_based_assessment(
    latest_records: List[DiabetesRecord],
    profile: DiabetesProfile,
    med_adherence_percent: float,
    missed_doses_7d: int,
    symptoms_list: Optional[List[str]] = None,
    systolic_bp: Optional[int] = None,
) -> dict:
    risk_level = "LOW"
    reasons: List[str] = []
    concerns: List[str] = []
    next_steps: List[str] = []
    monitor: List[str] = []

    if not latest_records:
        return {
            "risk_level": "LOW",
            "summary": "Hali qand ko'rsatkichi kiritilmagan. Muntazam o'lchash tavsiya qilinadi.",
            "key_concerns": ["Qand ko'rsatkichi ma'lumoti yo'q"],
            "recommended_next_steps": ["Glukometr bilan qand o'lchab kiritib boring"],
            "what_to_monitor": ["Qand ko'rsatkichi"],
            "doctor_note": "Ma'lumot yetarli emas. Shifokor bilan maslahatlashing.",
            "disclaimer": DISCLAIMER,
        }

    # ── Analyse latest glucose values ─────────────────────────────────────────
    latest = latest_records[0]
    glucose = float(latest.glucose_value)
    symptoms_text = (latest.symptoms or "").lower()

    # Very low glucose signals
    if glucose < _GLUCOSE_VERY_LOW:
        risk_level = max_risk(risk_level, "EMERGENCY")
        reasons.append(f"Qand ko'rsatkichi juda past signal: {glucose} mmol/L")
        concerns.append("Qand ko'rsatkichi xavfli darajada past ko'rinmoqda")
        next_steps.append("Tez shirinlik (sharbat, qand) iching va yaqiningizni xabardor qiling")
        next_steps.append("Agar hushdan ketish yoki titroq bo'lsa tez yordamga murojaat qiling")

    elif glucose < _GLUCOSE_LOW:
        risk_level = max_risk(risk_level, "HIGH")
        reasons.append(f"Qand ko'rsatkichi past signal: {glucose} mmol/L")
        concerns.append("Qand ko'rsatkichi odatdagidan past ko'rinmoqda")
        next_steps.append("Shifokor bilan maslahatlashing")

    elif glucose >= _GLUCOSE_VERY_HIGH:
        risk_level = max_risk(risk_level, "HIGH")
        reasons.append(f"Qand ko'rsatkichi yuqori signal: {glucose} mmol/L")
        concerns.append("Qand ko'rsatkichi odatdagidan sezilarli darajada yuqori ko'rinmoqda")
        next_steps.append("Shifokor bilan tezroq maslahatlashish tavsiya qilinadi")

    elif glucose >= _GLUCOSE_HIGH:
        risk_level = max_risk(risk_level, "MEDIUM")
        reasons.append(f"Qand ko'rsatkichi o'rtacha yuqori signal: {glucose} mmol/L")
        concerns.append("Qand ko'rsatkichi nazorat qilishni talab qiladi")
        next_steps.append("Keyingi o'lchovni kuzating va shifokorga xabar bering")

    elif glucose >= _GLUCOSE_MODERATE:
        risk_level = max_risk(risk_level, "LOW")
        reasons.append(f"Qand ko'rsatkichi biroz yuqoriroq: {glucose} mmol/L")

    # ── 7-day pattern analysis ─────────────────────────────────────────────────
    if len(latest_records) >= 3:
        recent_values = [float(r.glucose_value) for r in latest_records[:7]]
        high_count = sum(1 for v in recent_values if v >= _GLUCOSE_HIGH)
        if high_count >= 3:
            risk_level = max_risk(risk_level, "HIGH")
            concerns.append(f"So'nggi {len(recent_values)} ta o'lchovda {high_count} tasi yuqori signal ko'rsatdi")
            next_steps.append("Bir necha kun davomida yuqori ko'rsatkich — shifokorga murojaat qilish tavsiya qilinadi")

    # ── Symptoms analysis ──────────────────────────────────────────────────────
    danger_symptoms = ["hushdan ketish", "nafas qisishi", "ko'rish xiralashishi"]
    moderate_symptoms = ["bosh aylanishi", "terlash", "holsizlik", "chanqash"]

    for s in danger_symptoms:
        if s in symptoms_text:
            risk_level = max_risk(risk_level, "HIGH")
            concerns.append(f"Xavfli belgi qayd etildi: {s}")
            next_steps.append("Agar alomatlar kuchaysa tez yordamga murojaat qiling")

    moderate_count = sum(1 for s in moderate_symptoms if s in symptoms_text)
    if moderate_count >= 2:
        risk_level = max_risk(risk_level, "MEDIUM")
        concerns.append("Bir necha noqulay aломат qayd etildi")

    # ── Medication adherence ───────────────────────────────────────────────────
    if med_adherence_percent < 50 and missed_doses_7d >= 4:
        risk_level = max_risk(risk_level, "HIGH")
        concerns.append(f"Dori qabul qilish {med_adherence_percent:.0f}% ga tushdi, {missed_doses_7d} marta o'tkazib yuborildi")
        next_steps.append("Dori jadvalini ko'rib chiqing va shifokorga xabar bering")
    elif med_adherence_percent < 70:
        risk_level = max_risk(risk_level, "MEDIUM")
        concerns.append(f"Dori qabul qilish nazorat talab qiladi: {med_adherence_percent:.0f}%")
        monitor.append("Kunlik dori qabul qilish jadvali")

    # ── Blood pressure ─────────────────────────────────────────────────────────
    if systolic_bp is not None and systolic_bp >= 180:
        risk_level = max_risk(risk_level, "HIGH")
        concerns.append(f"Qon bosimi signali yuqori: {systolic_bp} mmHg")
        next_steps.append("Qon bosimini kuzating va shifokorga xabar bering")

    # ── Default monitoring items ───────────────────────────────────────────────
    monitor.extend([
        "Muntazam qand o'lchash (kuniga 1–2 marta)",
        "Dori vaqtida qabul qilish",
    ])
    if not next_steps:
        next_steps.append("Muntazam qand o'lchashni davom ettiring")
        next_steps.append("Dorilarni belgilangan vaqtda qabul qiling")

    summary_parts = reasons if reasons else ["Hozirgi ko'rsatkichlar nisbatan barqaror ko'rinmoqda"]
    summary = "; ".join(summary_parts)

    doctor_note = _build_doctor_note(risk_level, glucose, med_adherence_percent)

    return {
        "risk_level": risk_level,
        "summary": summary,
        "key_concerns": concerns or ["Ko'rsatkichlar kuzatilmoqda"],
        "recommended_next_steps": next_steps,
        "what_to_monitor": list(dict.fromkeys(monitor)),  # deduplicate preserving order
        "doctor_note": doctor_note,
        "disclaimer": DISCLAIMER,
    }


def _build_doctor_note(risk_level: str, glucose: float, adherence: float) -> str:
    if risk_level == "EMERGENCY":
        return (
            f"Bemor qand ko'rsatkichi {glucose} mmol/L signali bilan keldi va bir necha xavfli belgi qayd etildi. "
            "Darhol ko'rikdan o'tkazish tavsiya qilinadi."
        )
    elif risk_level == "HIGH":
        return (
            f"Qand ko'rsatkichi {glucose} mmol/L (signal). Dori qabul qilish: {adherence:.0f}%. "
            "Yaqin kunlarda ko'rikdan o'tkazish tavsiya qilinadi."
        )
    elif risk_level == "MEDIUM":
        return (
            f"Qand ko'rsatkichlari nazoratni talab qilmoqda. O'rtacha signal: {glucose} mmol/L, "
            f"dori qabul qilish: {adherence:.0f}%."
        )
    else:
        return (
            f"Ko'rsatkichlar barqaror ko'rinmoqda. Qand signali: {glucose} mmol/L, "
            f"dori qabul qilish: {adherence:.0f}%. Muntazam tekshiruv tavsiya qilinadi."
        )


def determine_trend_direction(records: List[DiabetesRecord]) -> str:
    """Compare last 3 days average vs previous 4 days average."""
    if len(records) < 4:
        return "STABLE"
    recent = [float(r.glucose_value) for r in records[:3]]
    older = [float(r.glucose_value) for r in records[3:7]]
    if not older:
        return "STABLE"
    recent_avg = sum(recent) / len(recent)
    older_avg = sum(older) / len(older)
    diff = recent_avg - older_avg
    if diff <= -1.0:
        return "IMPROVED"
    elif diff >= 2.0:
        return "WORSE"
    elif diff >= 1.0:
        return "NEEDS_ATTENTION"
    return "STABLE"
