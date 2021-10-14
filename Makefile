.PHONY: compile rules registry archivator clean

usage:
	@echo "usage: make TARGET"
	@echo 
	@echo "targets:"
	@echo "    all"
	@echo "    compile"
	@echo "    rules"
	@echo "    rules-archivator"
	@echo "    registry"
	@echo "    clean"

compile:
	tsc

rules:
	bin/make_rules.sh

rules-archivator:
	solid_auth.pl -w none -b https://hochstenbach.inrupt.net/archivator mirror /rules/ local/orchestrator/archivator

registry:
	node js/create_registry.js parse etc/demo.txt > local/institution/registry.ttl

all: compile rules registry

clean:
	node js/clean_data.js institution
