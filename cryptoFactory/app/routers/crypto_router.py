from fastapi import APIRouter
from tronpy.keys import PrivateKey
from base58 import b58encode_check

router = APIRouter()


@router.get("/api/crypto/trx/generate")
def generate_crypto():
    private_key = PrivateKey.random()
    public_key = private_key.public_key
    address = b58encode_check(public_key.to_address())

    return {"address": address.decode(), "private_key": str(private_key), "public_key": str(public_key)}
