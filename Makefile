.PHONY: compile watch rules registry archivator clean

usage:
	@echo "usage: make TARGET"
	@echo 
	@echo "local targets:"
	@echo "    all"
	@echo "    compile"
	@echo "    watch"
	@echo "    rules"
	@echo "    registry"
	@echo "    clean"
	@echo 
	@echo "remote targets:"
	@echo "    archivator-download"
	@echo "    archivator-upload"
	@echo "    archivator-clean"
	@echo
	@echo "    registrator-download"
	@echo "    registrator-upload"
	@echo "    registrator-clean"

compile:
	npx tsc

watch:
	npx tsc -w

rules:
	bin/make_rules.sh

registry:
	node js/create_registry.js parse etc/demo.txt > local/institution/registry.ttl

all: compile rules registry

clean:
	node js/clean_data.js institution

archivator-download:
	solid_auth.pl -w https://hochstenbach.inrupt.net/profile/card#me \
				  -b https://hochstenbach.inrupt.net \
				  mirror /archivator/rules/ local/orchestrator/archivator

archivator-upload:
	solid_auth.pl -w https://hochstenbach.inrupt.net/profile/card#me \
				  -b https://hochstenbach.inrupt.net \
				  -x \
				  upload local/orchestrator/archivator/ /archivator/rules/

archivator-clean:
	solid_auth.pl -w https://hochstenbach.inrupt.net/profile/card#me \
				  -b https://hochstenbach.inrupt.net \
				  -x clean /archivator/inbox/

	solid_auth.pl -w https://hochstenbach.inrupt.net/profile/card#me \
				  -b https://hochstenbach.inrupt.net \
				  -x clean /archivator/outbox/

	solid_auth.pl -w https://hochstenbach.inrupt.net/profile/card#me \
				  -b https://hochstenbach.inrupt.net \
				  -x clean /archivator/orchestrator/

	solid_auth.pl -w https://hochstenbach.inrupt.net/profile/card#me \
				  -b https://hochstenbach.inrupt.net \
				  -x clean /archivator/robustlinks/

registrator-download:
	solid_auth.pl -w https://hochstenbach.solidcommunity.net/profile/card#me \
				  -b https://hochstenbach.solidcommunity.net \
				  mirror /registrator/rules/ local/orchestrator/registrator

registrator-upload:
	solid_auth.pl -w https://hochstenbach.solidcommunity.net/profile/card#me \
				  -b https://hochstenbach.solidcommunity.net \
				  -x \
				  upload local/orchestrator/registrator/ /registrator/rules/

registrator-clean:
	solid_auth.pl -w https://hochstenbach.solidcommunity.net/profile/card#me \
				  -b https://hochstenbach.solidcommunity.net \
				  -x clean /registrator/inbox/

	solid_auth.pl -w https://hochstenbach.solidcommunity.net/profile/card#me \
				  -b https://hochstenbach.solidcommunity.net \
				  -x clean /registrator/outbox/

	solid_auth.pl -w https://hochstenbach.solidcommunity.net/profile/card#me \
				  -b https://hochstenbach.solidcommunity.net \
				  -x clean /registrator/orchestrator/

	solid_auth.pl -w https://hochstenbach.solidcommunity.net/profile/card#me \
				  -b https://hochstenbach.solidcommunity.net \
				  -x clean /registrator/librecat/

