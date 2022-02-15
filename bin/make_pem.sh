#!/bin/bash

CURVE="secp256r1"
DIR=$1

if [[ ! -d ${DIR} ]]; then
    echo "usage: $0 DIR"
    exit 1
fi

openssl ecparam -name ${CURVE} -genkey -noout | \
    openssl pkcs8 -topk8 -nocrypt -in /dev/stdin -out ${DIR}/private.pem

openssl ec -in ${DIR}/private.pem -pubout -out ${DIR}/public.pem