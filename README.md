# POrchestratorHelpers

A collection of Groocy and Nifi scripts required for the demonstrator

# Required 

- [Node](https://nodejs.org/en/)
- [Groovy](http://www.groovy-lang.org) - `brew install groovy`
- [TypeScript](https://www.typescriptlang.org) - `npm install -g typescript`
- [POrchestrator](https://github.com/MellonScholarlyCommunication/POrchestrator) - Prolog orchestrator

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

# Sol command line tool

This code inclides Jeff Zucker's https://www.npmjs.com/package/solid-shell command line
tool to administer Solid Pods. 

To run this tool your Solid pod must have "https://solid-node-client" as a trusted app.

For example in the Inrupt Pod

- Login into your pod
- In the right-top menu choose `Preferences`
- In the `Manage your trusted applications` add `https://solid-node-client`
- Give this client some access modes so that it can manage your pod (e.g. Read, Write,Append)

Set in your environment these variables:

- SOLID_USERNAME : e.g. `mypod`
- SOLID_PASSWORD 
- SOLID_IDP : e.g. `https://inrupt.net`
- SOLID_REMOTE_BASE : the base directory for all your requests, e.g. `https://mypod.inrupt.net`

Now you can run the `sol` command:

```
$ npx sol
```