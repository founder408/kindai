"""
Diabetes-focused Azure OpenAI service.
Falls back to rule_based if AI unavailable.
Never diagnoses, prescribes, or changes doses.
"""
import json
import httpx
from typing import Optional, List
from app.core.config import settings

DIABETES_SYSTEM_PROMPT = """Siz Kind AI dasturining sog'liq yordamchisisiz. Siz SHIFOKOR EMASSIZ.

Quyidagi qoidalarga qat'iy rioya qiling:
- Kasallik tashxisi QO'YMANG
- Dori yoki insulin dozasini BELGILAMANG
- "Sizda qand kasalligi bor" deb AYTMANG
- Muolaja TAVSIYA QILMANG

Faqat quyidagilarni bajaring:
- Qand ko'rsatkichlarini "signal" yoki "ko'rinmoqda" degan ehtiyotkorona so'zlar bilan baholang
- Xavf darajasini aniqlang (LOW / MEDIUM / HIGH / EMERGENCY)
- Shifokorga murojaat qilish tavsiyasi bering

Xavf darajalari (faqat signal sifatida):
- EMERGENCY: juda past qand ko'rsatkichi signali YOKI hushdan ketish/nafas qisishi alomatlari
- HIGH: ko'p marta yuqori ko'rsatkich signali YOKI dori o'tkazib yuborish + yuqori qand
- MEDIUM: o'rtacha yuqori ko'rsatkich yoki noto'g'ri dori rejimi
- LOW: barqaror ko'rsatkichlar, yaxshi dori rejimi

FAQAT quyidagi JSON formatini qaytaring (boshqa matn yo'q):
{
  "risk_level": "LOW|MEDIUM|HIGH|EMERGENCY",
  "summary": "o'zbek tilida qisqa baholash (1-2 gap)",
  "key_concerns": ["xavotir1", "xavotir2"],
  "recommended_next_steps": ["qadam1", "qadam2"],
  "what_to_monitor": ["kuzatuv1", "kuzatuv2"],
  "doctor_note": "shifokorga qisqa eslatma o'zbek tilida"
}"""


async def call_diabetes_ai(user_prompt: str) -> Optional[dict]:
    if not settings.AZURE_OPENAI_ENDPOINT or not settings.AZURE_OPENAI_API_KEY:
        return None

    url = (
        f"{settings.AZURE_OPENAI_ENDPOINT.rstrip('/')}"
        f"/openai/deployments/{settings.AZURE_OPENAI_DEPLOYMENT}"
        f"/chat/completions?api-version={settings.AZURE_OPENAI_API_VERSION}"
    )

    headers = {
        "Content-Type": "application/json",
        "api-key": settings.AZURE_OPENAI_API_KEY,
    }

    payload = {
        "messages": [
            {"role": "system", "content": DIABETES_SYSTEM_PROMPT},
            {"role": "user", "content": user_prompt},
        ],
        "temperature": 0.2,
        "max_tokens": 800,
        "response_format": {"type": "json_object"},
    }

    try:
        async with httpx.AsyncClient(timeout=30.0) as client:
            response = await client.post(url, headers=headers, json=payload)
            response.raise_for_status()

        data = response.json()
        content = data["choices"][0]["message"]["content"]
        result = json.loads(content)

        required = {"risk_level", "summary", "key_concerns", "recommended_next_steps",
                    "what_to_monitor", "doctor_note"}
        if not required.issubset(result.keys()):
            return None

        valid_levels = {"LOW", "MEDIUM", "HIGH", "EMERGENCY"}
        if result.get("risk_level") not in valid_levels:
            result["risk_level"] = "MEDIUM"

        # Ensure list fields are actually lists
        for list_field in ("key_concerns", "recommended_next_steps", "what_to_monitor"):
            if not isinstance(result.get(list_field), list):
                result[list_field] = [str(result.get(list_field, ""))]

        return result

    except Exception:
        return None


def build_diabetes_prompt(
    profile_name: str,
    profile_age: Optional[int],
    diabetes_type: Optional[str],
    glucose_records: List[dict],
    med_adherence_percent: float,
    missed_doses_7d: int,
    symptoms_recent: Optional[str],
    systolic_bp: Optional[int] = None,
    hc_steps: Optional[int] = None,
    previous_risk: Optional[str] = None,
) -> str:
    parts = [f"Bemor: {profile_name}"]
    if profile_age:
        parts.append(f"Yosh: {profile_age}")
    if diabetes_type:
        parts.append(f"Qand kasalligi turi: {diabetes_type}")

    if glucose_records:
        parts.append(f"\nSo'nggi {len(glucose_records)} ta qand o'lchovi:")
        for rec in glucose_records[:10]:
            ctx = rec.get("measurement_context", "RANDOM")
            val = rec.get("glucose_value", "?")
            ts = str(rec.get("measured_at", ""))[:16]
            parts.append(f"  - {val} mmol/L | {ctx} | {ts}")
    else:
        parts.append("\nQand o'lchov ma'lumoti yo'q")

    parts.append(f"\nDori qabul qilish (7 kun): {med_adherence_percent:.0f}%")
    parts.append(f"O'tkazib yuborilgan dori (7 kun): {missed_doses_7d} marta")

    if symptoms_recent:
        parts.append(f"So'nggi alomatlar: {symptoms_recent}")

    if systolic_bp:
        parts.append(f"Sistolik qon bosimi signali: {systolic_bp} mmHg")

    if hc_steps is not None:
        parts.append(f"Bugungi qadamlar (Health Connect): {hc_steps}")

    if previous_risk:
        parts.append(f"Oldingi xavf darajasi: {previous_risk}")

    return "\n".join(parts)
