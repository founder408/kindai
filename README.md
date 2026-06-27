# Kind AI — Qand va Sog'liq Boshqaruvi

> **Tibbiy eslatma:** Kind AI tibbiy tashxis qo'ymaydi, dori yoki insulin dozasini belgilamaydi. Bu tibbiy tashxis emas — zarur holatda shifokorga murojaat qiling.

Kind AI — O'zbekiston uchun mo'ljallangan raqamli sog'liq yordamchisi. Qand kasalligi bilan yashayotgan bemorlar va ularning yaqinlari uchun: glukoza nazorati, dori jadval, AI tahlil va favqulodda ogohlantirish — hammasini bitta ilovada.

## Loyiha tuzilishi

```
kind-ai/
├── app/                       # FastAPI backend
│   ├── models/                # SQLAlchemy 13 ta model
│   ├── routers/               # API endpointlar (diabetes + elderly)
│   ├── schemas/               # Pydantic validatsiya
│   └── services/              # AI, risk hisoblash, alertlar
├── alembic/versions/          # DB migratsiyalari 001-003
├── android/                   # Kotlin + Jetpack Compose
│   └── app/src/main/java/com/kindai/app/
│       ├── ui/screens/diabetes/    # 12 ta qand ekrani
│       ├── ui/screens/elderly_own/ # Keksa ota-ona ekranlar
│       └── data/                   # DTO, Retrofit, local storage
├── requirements.txt
└── .env.example
```

## Asosiy imkoniyatlar

**Qand kasalligi boshqaruvi (yangi):**
- Glukoza kiritish — mmol/L, kontekst (och qoringa/ovqatdan keyin), alomatlar
- Dori jadval — tabletka/insulin, mos kelish foizi %, seriya kuzatuvi
- AI tahlil — Azure OpenAI + qoida asosidagi zaxira (LOW/MEDIUM/HIGH/EMERGENCY)
- Tendensiyalar — kunlik grafik, 7/30/90 kunlik davr
- Shifokor xulosasi — to'liq davr, PDF tez orada
- Oila ulashish — g'amxo'r qo'shish, ruxsatni bekor qilish
- SOS tugmasi — 103 + yaqinlarga avtomatik ogohlantirish

**Keksa ota-ona monitoring (mavjud):**
- Smartwatch + telefon Health Connect ma'lumotlari
- Bolalar uchun real-vaqt dashboard
- AI xavf baholash va ogohlantirishlar

## Tech Stack

**Backend:** Python 3.11+, FastAPI, PostgreSQL, SQLAlchemy 2.x + Alembic, Pydantic v2, JWT, Azure OpenAI

**Android:** Kotlin, Jetpack Compose + Material 3, Retrofit, Navigation Compose, Health Connect

## Setup

### 1. Create virtual environment

```bash
python -m venv venv

# Windows
venv\Scripts\activate

# macOS/Linux
source venv/bin/activate
```

### 2. Install dependencies

```bash
pip install -r requirements.txt
```

### 3. Configure environment

```bash
cp .env.example .env
# Edit .env with your actual values
```

### 4. Run database migration

```bash
alembic upgrade head
```

### 5. Start development server

```bash
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

## API Documentation

After starting the server:
- Swagger UI: http://localhost:8000/docs
- ReDoc: http://localhost:8000/redoc

## API Endpoints

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login and get JWT token |
| GET | `/api/auth/me` | Get current user profile |

### Elderly Profiles
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/elderly` | Create elderly profile |
| GET | `/api/elderly` | List my elderly profiles |
| GET | `/api/elderly/{id}` | Get profile details |
| PUT | `/api/elderly/{id}` | Update profile |
| DELETE | `/api/elderly/{id}` | Delete profile |

### Medications
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/elderly/{id}/medications` | Add medication |
| GET | `/api/elderly/{id}/medications` | List medications |
| PUT | `/api/medications/{id}` | Update medication |
| DELETE | `/api/medications/{id}` | Delete medication |
| POST | `/api/medications/{id}/taken` | Mark as taken |

### Health Records
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/elderly/{id}/health-records` | Add record |
| GET | `/api/elderly/{id}/health-records` | List records |
| GET | `/api/elderly/{id}/health-records/latest` | Get latest |

### Watch Data
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/elderly/{id}/watch-data` | Send watch data |
| GET | `/api/elderly/{id}/watch-data` | List watch data |
| GET | `/api/elderly/{id}/watch-data/latest` | Get latest |

### AI Assessment
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/elderly/{id}/ai-assess` | Run AI assessment |
| GET | `/api/elderly/{id}/ai-assessments` | List assessments |
| GET | `/api/elderly/{id}/ai-assessments/latest` | Get latest |

### Alerts
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/alerts` | List my alerts |
| GET | `/api/elderly/{id}/alerts` | List elderly alerts |
| PUT | `/api/alerts/{id}/read` | Mark as read |

### Dashboard
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/elderly/{id}/dashboard` | Full dashboard summary |

### Health Check
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Server health check |

## Sample cURL Requests

### Register

```bash
curl -X POST http://localhost:8000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "full_name": "Alisher Karimov",
    "phone": "+998901234567",
    "password": "secret123",
    "role": "CHILD"
  }'
```

### Login

```bash
curl -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "login": "+998901234567",
    "password": "secret123"
  }'
```

### Create elderly profile

```bash
curl -X POST http://localhost:8000/api/elderly \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "full_name": "Karimov Abdulla",
    "age": 72,
    "gender": "male",
    "chronic_diseases": "Gipertoniya, Diabet tip 2",
    "emergency_phone": "+998901111111"
  }'
```

### Add medication

```bash
curl -X POST http://localhost:8000/api/elderly/ELDERLY_ID/medications \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Metformin",
    "dosage": "500mg",
    "instruction": "Ovqatdan keyin",
    "time_to_take": "08:00:00",
    "before_or_after_meal": "after"
  }'
```

### Add health record

```bash
curl -X POST http://localhost:8000/api/elderly/ELDERLY_ID/health-records \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "systolic_bp": 145,
    "diastolic_bp": 92,
    "blood_sugar": 7.8,
    "heart_rate": 82,
    "spo2": 96,
    "temperature": 36.6,
    "symptoms": "Bosh og'rig'i",
    "mood": "normal"
  }'
```

### Send watch data

```bash
curl -X POST http://localhost:8000/api/elderly/ELDERLY_ID/watch-data \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "heart_rate": 78,
    "steps": 3200,
    "sleep_hours": 6.5,
    "spo2": 97,
    "fall_detected": false,
    "battery": 65
  }'
```

### Run AI assessment

```bash
curl -X POST http://localhost:8000/api/elderly/ELDERLY_ID/ai-assess \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get dashboard

```bash
curl http://localhost:8000/api/elderly/ELDERLY_ID/dashboard \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Render Deployment

### Environment Variables on Render

Set these in your Render web service:
- `DATABASE_URL` - provided by Render PostgreSQL
- `JWT_SECRET` - strong random string
- `JWT_ALGORITHM` - HS256
- `ACCESS_TOKEN_EXPIRE_MINUTES` - 10080
- `AZURE_OPENAI_ENDPOINT` - your Azure endpoint
- `AZURE_OPENAI_API_KEY` - your Azure key
- `AZURE_OPENAI_DEPLOYMENT` - deployment name
- `AZURE_OPENAI_API_VERSION` - 2024-02-15-preview
- `CORS_ORIGINS` - your app domains

### Build Command

```bash
pip install -r requirements.txt && alembic upgrade head
```

### Start Command

```bash
uvicorn app.main:app --host 0.0.0.0 --port $PORT
```

## Medical Safety Disclaimer

This system does NOT provide medical diagnosis or prescribe medicine. It only classifies risk levels and recommends contacting family members, doctors, or emergency services when needed.

**"Bu tibbiy diagnoz emas. Zarur holatda shifokorga murojaat qiling."**
