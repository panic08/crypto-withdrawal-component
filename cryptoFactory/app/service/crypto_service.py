from tronpy.keys import PrivateKey
from base58 import b58encode_check
from eth_keys import keys
from eth_utils import to_checksum_address
from os import urandom


def generate_trx_data():
    private_key = PrivateKey.random()
    public_key = private_key.public_key
    address = b58encode_check(public_key.to_address())

    return {"address": address.decode(), "private_key": str(private_key), "public_key": str(public_key)}


def generate_eth_data():
    private_key = keys.PrivateKey(urandom(32))
    public_key = private_key.public_key
    address = to_checksum_address(public_key.to_canonical_address())

    return {"address": address, "private_key": private_key.to_hex(), "public_key": public_key.to_hex()}
