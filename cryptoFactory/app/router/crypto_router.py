from fastapi import APIRouter
from app.service.crypto_service import generate_trx_data, generate_eth_data
router = APIRouter()


@router.get("/api/crypto/trx/generate_data")
def generate_trx_data_route():
    trx_data = generate_trx_data()
    return trx_data


@router.get("/api/crypto/eth/generate_data")
def generate_eth_data_route():
    eth_data = generate_eth_data()
    return eth_data
