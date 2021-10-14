#!/bin/bash

URL=$1
RULES=$2

if [ $# -ne 2 ]; then
    echo "usage: $0 url-to-remote-notification rules"
    exit 1
fi

shift

TMPFILE=$(mktemp)

curl -H "Accept: application/ld+json" ${URL} > ${TMPFILE}

orchestrator/bin/orchestrator.sh ${TMPFILE} `pwd`/$@

rm ${TMPFILE}