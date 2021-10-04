#!/bin/bash

NAME=$1
BASE=${2:-local/institution}
ORCHESTRATOR=${3:-local/orchestrator}

if [ "${NAME}" == "" ]; then
    echo "usage: $0 name [base] [orchestrator]"
    exit 1
fi

# Make the LDP environment
mkdir -p ${BASE}/${NAME}/inbox
mkdir -p ${BASE}/${NAME}/events
mkdir -p ${BASE}/${NAME}/artefacts

touch ${BASE}/${NAME}/inbox/.gitignore
touch ${BASE}/${NAME}/events/.gitignore
touch ${BASE}/${NAME}/artefacts/.gitignore

cat > ${BASE}/${NAME}/card.ttl <<EOF
@prefix : <#>.
@prefix as: <http://www.w3.org/ns/activitystreams#>.
@prefix ex: <https://www.example.org/>.

:me 
    a         as:Person ;
    as:name   "${NAME}" ;
    as:inbox  <inbox> ;
    as:outbox <events> ;
    ex:artefacts <artefacts> ;
    ex:orchestrator <../o_${NAME}/card.ttl#me> .
EOF

# Make the orchestrator inbox
mkdir -p ${BASE}/o_${NAME}/inbox

touch ${BASE}/o_${NAME}/inbox/.gitignore

cat > ${BASE}/o_${NAME}/card.ttl <<EOF
@prefix : <#>.
@prefix as: <http://www.w3.org/ns/activitystreams#>.

:me 
    a         as:Service ;
    as:name   "${NAME}'s orchestrator" ;
    as:inbox  <inbox> .
EOF

# Make the orchestrator rules
mkdir -p ${ORCHESTRATOR}/${NAME}
