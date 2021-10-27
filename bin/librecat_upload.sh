#!/bin/bash

URL=$1
WEBID=$2

if [[ "${URL}" == "" ]] || [[ "${WEBID}" == "" ]]; then
    echo "usage: $0 URL WEBID"
    exit 1
fi

TMPFILE=$(mktemp)

# Fetch signposting information (if it exists)
node js/signposting_client.js -d get ${URL} describedBy > ${TMPFILE}

node js/librecat_api.js qae ${URL} ${WEBID} ${TMPFILE}

unlink ${TMPFILE}