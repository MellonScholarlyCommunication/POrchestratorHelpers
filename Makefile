.PHONY: compile rules

compile:
	tsc

rules:
	bin/make_rules.sh

all: compile rules