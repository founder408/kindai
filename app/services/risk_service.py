from typing import Optional
from app.models.health_record import HealthRecord
from app.models.watch_data import WatchData


def rule_based_risk_assessment(
    health: Optional[HealthRecord],
    watch: Optional[WatchData],
) -> dict:
    risk_level = "LOW"
    reasons = []

    if watch and watch.fall_detected:
        risk_level = "EMERGENCY"
        reasons.append("Yiqilish aniqlandi (fall detected)")

    if health:
        if health.spo2 is not None and health.spo2 <= 90:
            risk_level = "EMERGENCY"
            reasons.append(f"SpO2 juda past: {health.spo2}%")

        if health.systolic_bp is not None and health.systolic_bp >= 180:
            risk_level = max_risk(risk_level, "HIGH")
            reasons.append(f"Sistolik bosim yuqori: {health.systolic_bp}")

        if health.diastolic_bp is not None and health.diastolic_bp >= 110:
            risk_level = max_risk(risk_level, "HIGH")
            reasons.append(f"Diastolik bosim yuqori: {health.diastolic_bp}")

        if health.heart_rate is not None and health.heart_rate >= 125:
            risk_level = max_risk(risk_level, "HIGH")
            reasons.append(f"Yurak urishi tez: {health.heart_rate} bpm")

    if watch:
        if watch.spo2 is not None and watch.spo2 <= 90:
            risk_level = "EMERGENCY"
            reasons.append(f"Soat SpO2 juda past: {watch.spo2}%")

        if watch.heart_rate is not None and watch.heart_rate >= 125:
            risk_level = max_risk(risk_level, "HIGH")
            reasons.append(f"Soat yurak urishi tez: {watch.heart_rate} bpm")

        if (
            watch.sleep_hours is not None
            and watch.sleep_hours < 4
            and watch.heart_rate is not None
            and watch.heart_rate > 110
        ):
            risk_level = max_risk(risk_level, "MEDIUM")
            reasons.append("Kam uxlash + tez yurak urishi")

    if not reasons:
        reasons.append("Barcha ko'rsatkichlar normal doirada")

    summary = "; ".join(reasons)
    recommendation = _get_recommendation(risk_level)

    return {
        "risk_level": risk_level,
        "summary": summary,
        "recommendation": recommendation,
    }


def check_watch_battery_alert(watch: Optional[WatchData]) -> Optional[dict]:
    if watch and watch.battery is not None and watch.battery <= 15:
        return {
            "type": "WATCH_RISK",
            "title": "Soat batareyasi past",
            "message": f"Soat batareyasi {watch.battery}% ga tushdi. Iltimos zaryadlang.",
            "severity": "WARNING",
        }
    return None


def max_risk(current: str, new: str) -> str:
    levels = {"LOW": 0, "MEDIUM": 1, "HIGH": 2, "EMERGENCY": 3}
    if levels.get(new, 0) > levels.get(current, 0):
        return new
    return current


def _get_recommendation(risk_level: str) -> str:
    disclaimer = "Bu tibbiy diagnoz emas. Zarur holatda shifokorga murojaat qiling."
    if risk_level == "EMERGENCY":
        return f"DARHOL tez yordam chaqiring yoki yaqin kasalxonaga olib boring! {disclaimer}"
    elif risk_level == "HIGH":
        return f"Shifokorga murojaat qilish tavsiya etiladi. Oila a'zolarini xabardor qiling. {disclaimer}"
    elif risk_level == "MEDIUM":
        return f"Holat kuzatilmoqda. Agar yomonlashsa, shifokorga murojaat qiling. {disclaimer}"
    else:
        return f"Holat barqaror. Muntazam tekshiruvni davom ettiring. {disclaimer}"
