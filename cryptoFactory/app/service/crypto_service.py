from tronpy.keys import PrivateKey
from bitcoinlib.keys import Key, Address
from eth_account import Account
from base58 import b58encode_check


def generate_trx_data():
    private_key = PrivateKey.random()
    public_key = private_key.public_key
    address = b58encode_check(public_key.to_address())

    return {"address": address.decode(), "private_key": str(private_key), "public_key": str(public_key)}


def generate_eth_data():
    account = Account.create()

    private_key = account._private_key.hex()
    public_key = account._key_obj.public_key.to_hex()
    address = account.address

    return {"address": address, "private_key": private_key, "public_key": public_key}


def generate_btc_data():
    private_key = Key()
    public_key = private_key.public()
    address = Address(public_key.hex(), encoding='bech32', script_type='p2wpkh').address

    return {"address": address, "private_key": private_key.private_hex, "public_key": public_key.hex()}


