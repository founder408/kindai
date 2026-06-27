from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.core.config import settings
from app.routers import auth, elderly, elderly_self, medications, health_records, watch_data, ai, alerts, dashboard
from app.routers import diabetes_self, diabetes_caregiver

app = FastAPI(
    title=settings.APP_NAME,
    description="Kind AI — Diabetes Management & Family Health Platform",
    version="2.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins_list,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(auth.router)

# Diabetes routes — /api/diabetes/me/* must be before /api/diabetes/profiles/{id}/*
app.include_router(diabetes_self.router)
app.include_router(diabetes_caregiver.router)

# Legacy elderly routes (kept for backward compatibility)
app.include_router(elderly_self.router)  # /api/elderly/me/* before /api/elderly/{id}
app.include_router(elderly.router)
app.include_router(medications.router)
app.include_router(health_records.router)
app.include_router(watch_data.router)
app.include_router(ai.router)
app.include_router(alerts.router)
app.include_router(dashboard.router)


@app.get("/health", tags=["Health"])
def health_check():
    return {"status": "ok", "app": settings.APP_NAME, "version": "1.0.0"}
