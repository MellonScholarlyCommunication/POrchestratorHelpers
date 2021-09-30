# Local Nifi directories and file

## cache

A persistent storage for the Nifi Distributed Hashmap (deduping incoming inbox ids)

## instutution

A directory that contains all pods defined by an institution

- _NAME_ : the pod for a person or software tool
- o _NAME_ : an LDN inbox for the orchestrator of a pod
- registry.ttl : a FOAF list of all known pods 

## ldn-sender

A directory for mass sending of notifications via Nifi

## orchestrator

The N3 rules for each orchestrator