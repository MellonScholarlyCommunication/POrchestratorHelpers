#!/bin/bash

BASE=local
ORG=institution
NAME=$1

if [ "${NAME}" == "" ]; then
    echo "usage: $0 name"
    exit 1
fi

# Make the LDP environment
mkdir -p ${BASE}/${ORG}/${NAME}/inbox
mkdir -p ${BASE}/${ORG}/${NAME}/events
mkdir -p ${BASE}/${ORG}/${NAME}/artefacts

touch ${BASE}/${ORG}/${NAME}/inbox/.gitignore
touch ${BASE}/${ORG}/${NAME}/events/.gitignore
touch ${BASE}/${ORG}/${NAME}/artefacts/.gitignore

cat > ${BASE}/${ORG}/${NAME}/card.ttl <<EOF
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
mkdir -p ${BASE}/${ORG}/o_${NAME}/inbox

touch ${BASE}/${ORG}/o_${NAME}/inbox/.gitignore

cat > ${BASE}/${ORG}/o_${NAME}/card.ttl <<EOF
@prefix : <#>.
@prefix as: <http://www.w3.org/ns/activitystreams#>.

:me 
    a         as:Service ;
    as:name   "${NAME}'s orchestrator" ;
    as:inbox  <inbox> .
EOF

# Make the orchestrator rules
mkdir -p ${BASE}/orchestrator/${NAME}
