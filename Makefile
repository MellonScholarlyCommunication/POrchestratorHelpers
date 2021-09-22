.PHONY: compile n3 registry clean

compile:
	tsc

n3:
	bin/make_rules.sh

registry:
	node js/create_registry.js > local/institution/registry.ttl

all: compile rules registry

clean:
	node js/clean_data.js institution