.PHONY: compile n3 clean

compile:
	tsc

n3:
	bin/make_rules.sh

all: compile rules

clean:
	node js/clean_data.js institution