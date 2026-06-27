<div align="center">

# 🩺 Kind AI

### Aqlli Sog'liq Yordamchisi — O'zbekiston uchun

**Qand kasalligi nazorati · Dori jadval · AI tahlil · Oila ulashish**

---

[![Android](https://img.shields.io/badge/Android-Kotlin%20%2B%20Compose-brightgreen?logo=android)](https://developer.android.com)
[![Backend](https://img.shields.io/badge/Backend-FastAPI%20%2B%20PostgreSQL-blue?logo=fastapi)](https://fastapi.tiangolo.com)
[![AI](https://img.shields.io/badge/AI-Azure%20OpenAI%20GPT--4o-purple?logo=openai)](https://azure.microsoft.com/en-us/products/ai-services/openai-service)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

<br/>

> ⚕️ **Tibbiy muhim eslatma:** Kind AI tibbiy tashxis qo'ymaydi, dori yoki insulin dozasini belgilamaydi.
> Zarur holatda shifokorga murojaat qiling.

</div>

---

## 👥 Jamoa — Ilg'or

| Rol | Mas'uliyat |
|-----|-----------|
| **Jamoa nomi** | Ilg'or |
| **Loyiha nomi** | Kind AI |
| **Yo'nalish** | HealthTech / AI — O'zbekiston |

---

## 💡 Muammo va Yechim

**Muammo:** O'zbekistonda 3+ million qand kasalligi bilan yashayotgan bemorlar mavjud. Ularning aksariyati:
- Glukoza darajasini qog'ozga yozib kuzatadi yoki umuman kuzatmaydi
- Dori qabul jadvalini unutadi
- Shifokorga borishdan avval o'z holatini tushunmaydi
- Yaqinlari bilan sog'lik ma'lumotlarini ulasha olmaydi

**Yechim — Kind AI:** Bir ilovada barcha zarur vositalar — glukoza kuzatuvi, dori eslatmasi, sun'iy intellekt tahlili va oila ulashish. O'zbek tilida, mahalliy foydalanuvchiga moslashtirilgan.

---

## 📱 Skrinshtlar

> *Demo video va skrinshtlar qo'shilmoqda...*

---

## ✨ Asosiy Imkoniyatlar

### 🔴 Qand Kasalligi Boshqaruvi
| Imkoniyat | Tavsif |
|-----------|--------|
| **Glukoza kuzatuvi** | mmol/L kiritish, o'lchov konteksti (och qoringa, ovqatdan keyin), alomatlar qayd etish |
| **Dori jadval** | Tabletka / Insulin jadval, kunlik mos kelish foizi, ko'p kunlik seriya kuzatuvi |
| **AI Tahlil** | Azure OpenAI GPT-4o + qoida asosidagi zaxira; 4 xavf darajasi (LOW → EMERGENCY) |
| **Tendensiyalar** | 7 / 30 / 90 kunlik grafik, o'rtacha, eng yuqori/past ko'rsatkichlar |
| **Shifokor xulosasi** | To'liq davr hisoboti, alomatlar, adherence, AI natijalari (PDF tez orada) |
| **Oila ulashish** | G'amxo'r qo'shish, ruxsatni istalgan vaqtda bekor qilish (consent-based) |
| **SOS tugmasi** | 103 tez yordam + yaqinlarga avtomatik ogohlantirish xabari |

### 👴 Keksa Ota-Ona Monitoring
| Imkoniyat | Tavsif |
|-----------|--------|
| **Sog'liq yozuvlari** | Qon bosimi, qon shakari, yurak urishi, SpO2, harorat |
| **Smartwatch integratsiya** | Heart rate, qadamlar, uyqu, yiqilish aniqlash |
| **Health Connect** | Android telefon sog'lik ma'lumotlarini avtomatik o'qish |
| **Bolalar dashboard** | Real-vaqt monitoring, ogohlantirishlar, AI baholash |
| **AI Xavf Baholash** | Har bir o'lchov uchun avtomatik xavf darajasi va tavsiyalar |

---

## 🏗️ Arxitektura

```
kind-ai/
├── 📁 app/                         # FastAPI Backend
│   ├── models/                     # 13 ta SQLAlchemy model
│   │   ├── user.py
│   │   ├── elderly.py, medication.py, health_record.py ...
│   │   ├── diabetes_profile.py     # Diabetes profil
│   │   ├── diabetes_record.py      # Glukoza o'lchovlari
│   │   ├── diabetes_medication.py  # Dori + qabul loglari
│   │   ├── diabetes_ai_assessment.py
│   │   └── diabetes_alert.py
│   ├── routers/                    # REST API endpointlar
│   │   ├── auth.py                 # JWT autentifikatsiya
│   │   ├── diabetes_self.py        # 19 ta /api/diabetes/me/* endpoint
│   │   ├── diabetes_caregiver.py   # G'amxo'r kirish
│   │   ├── elderly.py, health_records.py ...
│   ├── services/
│   │   ├── diabetes_ai_service.py  # Azure OpenAI integratsiya
│   │   └── diabetes_risk_service.py # Qoida asosidagi xavf hisoblash
│   └── schemas/diabetes.py         # 22 ta Pydantic validatsiya
│
├── 📁 alembic/versions/            # Ma'lumotlar bazasi migratsiyalari
│   ├── 001_initial_schema.py
│   ├── 002_add_elderly_accounts.py
│   └── 003_diabetes_pivot.py       # Additive (DROP yo'q)
│
└── 📁 android/                     # Kotlin Android Ilovasi
    └── app/src/main/java/com/kindai/app/
        ├── ui/screens/diabetes/    # 12 ta qand ekrani
        │   ├── DiabetesHomeScreen.kt
        │   ├── AddGlucoseScreen.kt
        │   ├── GlucoseHistoryScreen.kt
        │   ├── DiabetesMedicationScreen.kt
        │   ├── DiabetesAiScreen.kt
        │   ├── DiabetesTrendsScreen.kt
        │   ├── DoctorSummaryScreen.kt
        │   ├── DiabetesAlertsScreen.kt
        │   ├── DiabetesSosScreen.kt
        │   └── FamilySharingScreen.kt
        ├── ui/screens/elderly_own/ # Keksa ota-ona ekranlar
        ├── ui/components/          # Qayta ishlatiladigan UI komponentlar
        ├── ui/design/              # KindColors, KindShapes, KindSpacing
        ├── ui/navigation/          # AppNavigation + Routes
        └── data/                   # DTO, Retrofit, TokenManager
```

---

## 🛠️ Texnologiyalar

### Backend
| Texnologiya | Versiya | Maqsad |
|-------------|---------|--------|
| **Python** | 3.11+ | Asosiy til |
| **FastAPI** | 0.111 | REST API framework |
| **PostgreSQL** | 15 | Asosiy ma'lumotlar bazasi |
| **SQLAlchemy** | 2.0 | ORM |
| **Alembic** | 1.13 | DB migratsiyalari |
| **Azure OpenAI** | GPT-4o | AI tahlil |
| **JWT** (python-jose) | 3.3 | Autentifikatsiya |
| **Pydantic** | v2 | Ma'lumot validatsiyasi |

### Android
| Texnologiya | Versiya | Maqsad |
|-------------|---------|--------|
| **Kotlin** | 1.9+ | Asosiy til |
| **Jetpack Compose** | 1.6+ | Declarative UI |
| **Material Design 3** | — | Design system |
| **Retrofit 2** | — | HTTP klient |
| **Navigation Compose** | — | Ekranlar orasida navigatsiya |
| **Health Connect** | — | Telefon sog'lik ma'lumotlari |

---

## 🚀 Ishga Tushirish

### Backend

```bash
# 1. Virtual muhit
python -m venv venv
venv\Scripts\activate        # Windows
# source venv/bin/activate   # macOS/Linux

# 2. Kutubxonalar
pip install -r requirements.txt

# 3. Muhit sozlamasi
cp .env.example .env
# .env faylini tahrirlang (DB, JWT, Azure sozlamalari)

# 4. Ma'lumotlar bazasi
alembic upgrade head

# 5. Server
uvicorn app.main:app --reload --host 0.0.0.0 --port 8001
```

### Android

```bash
# Android Studio da oching: android/ papkasini
# yoki terminaldan:

cd android
.\gradlew.bat assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### Muhit o'zgaruvchilari (`.env`)

```env
DATABASE_URL=postgresql://user:password@host:5432/dbname
JWT_SECRET=your-strong-secret-key
JWT_ALGORITHM=HS256
ACCESS_TOKEN_EXPIRE_MINUTES=10080

AZURE_OPENAI_ENDPOINT=https://your-resource.openai.azure.com/
AZURE_OPENAI_API_KEY=your_api_key
AZURE_OPENAI_DEPLOYMENT_NAME=your_deployment
AZURE_OPENAI_API_VERSION=2024-02-15-preview
```

---

## 🔌 API Endpointlar

### Autentifikatsiya
```
POST /api/auth/register     — Ro'yxatdan o'tish
POST /api/auth/login        — Kirish (JWT token)
GET  /api/auth/me           — Joriy foydalanuvchi
```

### Qand Kasalligi — Shaxsiy (`/api/diabetes/me/`)
```
POST/GET  /profile                  — Profil yaratish / olish
GET       /dashboard                — To'liq dashboard
POST/GET  /glucose                  — Glukoza kiritish / tarixi
GET       /glucose/trends           — Tendensiyalar
POST/GET  /medications              — Dorilar
POST      /medications/{id}/taken   — Dori qabul qilindi/o'tkazildi
GET       /medication-adherence     — Mos kelish foizi
POST      /ai-assess                — AI tahlil ishga tushirish
GET       /alerts                   — Ogohlantirishlar
POST      /sos                      — SOS yuborish
GET       /doctor-summary           — Shifokor xulosasi
```

### Keksa Ota-Ona (`/api/elderly/{id}/`)
```
POST/GET  /health-records     — Sog'lik yozuvlari
POST/GET  /medications        — Dorilar
POST/GET  /watch-data         — Smartwatch ma'lumotlari
POST      /ai-assess          — AI xavf baholash
GET       /dashboard          — To'liq dashboard
```

> 📄 Swagger UI: `http://localhost:8001/docs`

---

## 🔒 Xavfsizlik

- `.env` fayli `.gitignore` orqali himoyalangan — GitHub ga **hech qachon** yuklanmaydi
- JWT tokenlar 7 kun amal qiladi
- G'amxo'r faqat bemor ruxsati bilan (`consent_confirmed=true`) ma'lumotlarni ko'ra oladi
- Barcha endpointlar `Authorization: Bearer <token>` talab qiladi
- SQL injection: SQLAlchemy ORM parametrik so'rovlar

---

## ⚕️ Tibbiy Xavfsizlik Prinsiplari

Kind AI quyidagilarni **HECH QACHON** qilmaydi:

| ❌ Taqiqlangan | ✅ Kind AI nima qiladi |
|---------------|----------------------|
| Tibbiy tashxis qo'yish | Xavf darajasini bildiradi (signal) |
| Dori tayinlash | Mavjud dori jadvalini kuzatadi |
| Insulin dozasini o'zgartirish | Faqat yozib olingan dozani eslatadi |
| Shifokorni almashtirish | Shifokorga murojaat etishni tavsiya qiladi |

**Har bir AI natijasi yonida disclaimer ko'rsatiladi:**
> *"Bu tibbiy tashxis emas. Zarur holatda shifokorga murojaat qiling. Kind AI dori yoki insulin dozasini belgilamaydi."*

---

## 📊 Glukoza Xavf Darajalari

| Daraja | Qiymat (mmol/L) | Rang | Tavsif |
|--------|----------------|------|--------|
| 🟢 Xavfsiz | 4.0 – 8.0 | Yashil | Normal ko'rsatkich |
| 🟡 O'rta | 8.0 – 11.1 | Sariq | Kuzatuv tavsiya |
| 🟠 Yuqori | 11.1 – 16.7 | To'q sariq | Shifokorga murojaat |
| 🔴 Juda yuqori | ≥ 16.7 | Qizil | Zudlik bilan harakat |
| 🚨 Juda past | < 3.0 | Qoʻngʻir qizil | FAVQULODDA |

---

## 📁 Ma'lumotlar Bazasi Migratsiyalari

```
001_initial_schema.py     — Asosiy jadvallar (users, elderly, medications, ...)
002_add_elderly_accounts.py — Keksa hisoblar
003_diabetes_pivot.py     — Qand kasalligi (6 ta yangi jadval, additive only)
```

> Migration 003 faqat `CREATE TABLE` / `ADD COLUMN` — mavjud ma'lumotlarni o'zgartirmaydi.

---

## 🤝 Hissa Qo'shish

```bash
git clone https://github.com/your-username/kind-ai.git
cd kind-ai
git checkout -b feature/your-feature
# o'zgartirishlar kiriting
git commit -m "feat: your feature description"
git push origin feature/your-feature
# Pull Request oching
```

---

## 📄 Litsenziya

MIT License — ta'lim, tadqiqot va tijoriy maqsadlarda foydalanish uchun erkin.

---

<div align="center">

**Jamoa Ilg'or** tomonidan ❤️ bilan yaratildi

*Kind AI — Sog'ligingizni nazorat qiling, hayotingizni yaxshilang*

</div>
