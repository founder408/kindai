# Kind AI - Android App

AI-powered elderly care and health monitoring app for families and caregivers.

## Tech Stack

- Kotlin
- Jetpack Compose (Material 3)
- MVVM / Clean Architecture
- Retrofit + OkHttp
- DataStore Preferences
- Navigation Compose
- Kotlin Coroutines + StateFlow

## Setup

### 1. Open in Android Studio

Open the `android/` directory as an Android Studio project.

### 2. Configure Backend URL

Edit `app/src/main/java/com/kindai/app/core/Constants.kt`:

```kotlin
const val BASE_URL = "http://10.0.2.2:8000/"  // For emulator
// const val BASE_URL = "http://192.168.1.X:8000/"  // For real device on same network
// const val BASE_URL = "https://your-app.onrender.com/"  // For deployed backend
```

### 3. Start Backend

```bash
cd ../  # root project directory
source venv/bin/activate  # or venv\Scripts\activate on Windows
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

### 4. Run App

- Select an emulator or connected device
- Click Run (Shift+F10)

## Important Notes

### Emulator
- Use `http://10.0.2.2:8000/` as BASE_URL (maps to host machine localhost)

### Real Device
- Backend and device must be on same network
- Use your computer's local IP: `http://192.168.x.x:8000/`
- Or use a deployed backend URL

### Deployed Backend
- Set BASE_URL to your Render deployment URL

## Demo Flow

1. Launch app → Onboarding screen (accept safety/consent)
2. Register as CHILD / Login
3. Create elderly profile (with consent checkbox)
4. Open dashboard
5. Send watch data → Select "Yiqilish demo" preset
6. Run AI assessment → See HIGH/EMERGENCY risk
7. View alerts
8. Open "Oddiy rejim" for elderly-friendly interface

## Medical & Ethical Disclaimer

- This app does NOT diagnose diseases
- This app does NOT prescribe medication
- AI only classifies risk levels and provides recommendations
- Emergency situations require calling actual emergency services (103)
- User must have consent/legal basis to monitor another person's health

**"Bu tibbiy diagnoz emas. Zarur holatda shifokorga murojaat qiling."**

## Privacy

- JWT tokens stored securely in DataStore
- No secrets hardcoded in the app
- All AI processing happens on the backend
- Azure OpenAI key is never exposed to the mobile app
- Health data treated as sensitive
