.PHONY: compile rules registry archivator clean

usage:
	@echo "usage: make TARGET"
	@echo 
	@echo "targets:"
	@echo "    all"
	@echo "    compile"
	@echo "    rules"
	@echo "    registry"
	@echo "    clean"

compile:
	tsc

rules:
	bin/make_rules.sh

registry:
	node js/create_registry.js parse --baseurl=http://localhost:2000/ etc/demo.txt > local/institution/registry.ttl

all: compile rules registry

clean:
	node js/clean_data.js institution
