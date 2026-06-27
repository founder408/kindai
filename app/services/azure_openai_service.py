import json
import httpx
from typing import Optional
from app.core.config import settings

SYSTEM_PROMPT = """You are a safety monitoring assistant for elderly care. You are NOT a doctor.
Do NOT diagnose any disease. Do NOT prescribe any medicine.

Analyze the given elderly health data and classify risk level.
Return ONLY valid JSON with exactly these fields:
{
  "risk_level": "LOW | MEDIUM | HIGH | EMERGENCY",
  "summary": "brief description of the situation in Uzbek language",
  "recommendation": "what action to take in Uzbek language"
}

Risk classification rules:
- EMERGENCY: fall detected, SpO2 <= 90, or critical vital signs
- HIGH: systolic BP >= 180, diastolic BP >= 110, heart rate >= 125
- MEDIUM: mild abnormalities, poor sleep + elevated heart rate
- LOW: all vitals within normal range

Always include this disclaimer in the recommendation:
"Bu tibbiy diagnoz emas. Zarur holatda shifokorga murojaat qiling."

Respond ONLY with the JSON object. No markdown, no code blocks, no extra text."""


async def call_azure_openai(user_prompt: str) -> Optional[dict]:
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
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "user", "content": user_prompt},
        ],
        "temperature": 0.3,
        "max_tokens": 500,
        "response_format": {"type": "json_object"},
    }

    try:
        async with httpx.AsyncClient(timeout=30.0) as client:
            response = await client.post(url, headers=headers, json=payload)
            response.raise_for_status()

        data = response.json()
        content = data["choices"][0]["message"]["content"]
        result = json.loads(content)

        if "risk_level" in result and "summary" in result and "recommendation" in result:
            valid_levels = {"LOW", "MEDIUM", "HIGH", "EMERGENCY"}
            if result["risk_level"] not in valid_levels:
                result["risk_level"] = "MEDIUM"
            return result

        return None
    except Exception:
        return None


def build_ai_prompt(
    elderly_name: str,
    elderly_age: Optional[int],
    chronic_diseases: Optional[str],
    health_record: Optional[dict],
    watch_data: Optional[dict],
    medication_status: Optional[str],
) -> str:
    prompt_parts = [f"Elderly person: {elderly_name}"]

    if elderly_age:
        prompt_parts.append(f"Age: {elderly_age}")
    if chronic_diseases:
        prompt_parts.append(f"Chronic diseases: {chronic_diseases}")

    if health_record:
        prompt_parts.append("\nLatest health record:")
        for key, value in health_record.items():
            if value is not None:
                prompt_parts.append(f"  - {key}: {value}")

    if watch_data:
        prompt_parts.append("\nLatest smart watch data:")
        for key, value in watch_data.items():
            if value is not None:
                prompt_parts.append(f"  - {key}: {value}")

    if medication_status:
        prompt_parts.append(f"\nMedication status: {medication_status}")

    return "\n".join(prompt_parts)
