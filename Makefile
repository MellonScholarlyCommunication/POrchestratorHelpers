.PHONY: prod dev compile

compile:
	tsc

prod:
	for f in n3/*.n3; do \
		S=$$(echo $$f | sed -e 's/n3\//local\/orchestrator\/alice\//'); \
		echo "$$f > $$S" ; \
		npm exec mustache etc/alice-prod.json $$f > $$S ; \
	done ;\
	for f in n3/*.n3; do \
		S=$$(echo $$f | sed -e 's/n3\//local\/orchestrator\/bob\//'); \
		echo "$$f > $$S" ; \
		npm exec mustache etc/bob-prod.json $$f > $$S ; \
	done

dev:
	for f in n3/*.n3; do \
		S=$$(echo $$f | sed -e 's/n3\//local\/orchestrator\/alice\//'); \
		echo "$$f > $$S" ; \
		npm exec mustache etc/alice-dev.json $$f > $$S ; \
	done ;\
	for f in n3/*.n3; do \
		S=$$(echo $$f | sed -e 's/n3\//local\/orchestrator\/bob\//'); \
		echo "$$f > $$S" ; \
		npm exec mustache etc/bob-dev.json $$f > $$S ; \
	done