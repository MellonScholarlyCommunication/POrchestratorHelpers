.PHONY: compile rules registry clean

compile:
	tsc

rules:
	bin/make_rules.sh

registry:
	node js/create_registry.js > local/institution/registry.ttl

all: compile rules registry

clean:
	node js/clean_data.js institution