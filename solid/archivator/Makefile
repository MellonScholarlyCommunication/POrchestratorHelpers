.PHONY: clean acl files

usage:
	@echo "usage: make TARGET"
	@echo 
	@echo "targets:"
	@echo "    acl"
	@echo "    clean"
	@echo "    files"

clean:
	npx sol -l run ./clean_archivator.sol

acl:
	SOLID_REMOTE_BASE=https://hochstenbach.inrupt.net/archivator npx sol -l run ./set_read_acl.sol
	SOLID_REMOTE_BASE=https://hochstenbach.inrupt.net/archivator/profile npx sol -l run ./set_read_acl.sol
	SOLID_REMOTE_BASE=https://hochstenbach.inrupt.net/archivator/rules npx sol -l run ./set_read_acl.sol
	SOLID_REMOTE_BASE=https://hochstenbach.inrupt.net/archivator/inbox npx sol -l run ./set_append_acl.sol
	SOLID_REMOTE_BASE=https://hochstenbach.inrupt.net/archivator/outbox npx sol -l run ./set_append_acl.sol
	SOLID_REMOTE_BASE=https://hochstenbach.inrupt.net/archivator/robustlinks npx sol -l run ./set_append_acl.sol
	SOLID_REMOTE_BASE=https://hochstenbach.inrupt.net/archivator/orchestrator npx sol -l run ./set_append_acl.sol

files:
	npx sol zip ./profile/ ./profile.zip
	npx sol -l unzip ./profile.zip /archivator/
	rm ./profile.zip
	mkdir rules
	cp ../../local/orchestrator/archivator/*.n3 rules/
	npx sol zip ./rules/ ./rules.zip
	npx sol -l unzip ./rules.zip /archivator/
	rm -rf ./rules.zip rules