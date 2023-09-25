import uvicorn
from fastapi import FastAPI
from app.router.crypto_router import router as crypto_router
from fastapi.middleware.cors import CORSMiddleware
app = FastAPI()

origins = [
    "http://localhost:8080"
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(crypto_router, prefix="")

if __name__ == '__main__':
    uvicorn.run(app, host="0.0.0.0", port=8082)
