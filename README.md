# POrchestratorHelpers

A collection of Groocy and Nifi scripts required for the demonstrator

# Required 

- [Node](https://nodejs.org/en/)
- [Groovy](http://www.groovy-lang.org) - `brew install groovy`
- [TypeScript](https://www.typescriptlang.org) - `npm install -g typescript`
- [POrchestrator](https://github.com/MellonScholarlyCommunication/POrchestrator) - Prolog orchestrator
- [JQ](https://stedolan.github.io/jq/) - `brew install jq`

# Install

```
ln -s ../POrchestrator orchestrator
npm install
make compile
```

# Usage

All `groovy/nifi_*.groovy` are Nifi processors that can be used in the [Mellon Nifi Demonstrator](https://github.com/MellonScholarlyCommunication).

All `groovy/example_*.groovy` are command line tools that demostrate the capabilities of the Nifi processors. E.g.

```
groovy/example_reader.groovy mydata.ttl NT > mydata.nt
```

# Optional

## Solid_auth.pl tool

The demonstrator uses `solid_auth.pl` to periodically clean an mirror
Pods.

This Perl program can be installed from https://metacpan.org/pod/Web::Solid::Auth

See https://metacpan.org/pod/Web::Solid::Auth for instructions