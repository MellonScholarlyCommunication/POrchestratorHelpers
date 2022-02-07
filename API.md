# POrchestrator API

## Authentication

All request to the POrchestrator API need a valid license_key set in the 
`Authorization` HTTP Header:

```
$ curl -H 'Authorization: Bearer base64(license_key)' ...
```

## / - List/Add orchestrators

```
GET /
```

```
$ curl http://localhost:9190/ | json_pp
[
   {
      "version" : 0,
      "name" : "Copy of Copy of Orchestrator - http://localhost:2000/alice",
      "stopped" : 49,
      "comments" : "",
      "running" : 0,
      "disabled" : 0,
      "id" : "e7b1b8ef-6ebf-32ee-73e0-0fe97b4a0e20",
      "variables" : {
         "ldp.events" : "http://localhost:2000/alice/events",
         "ldn.inbox" : "http://localhost:2000/alice/inbox",
         "orchestrator.inbox" : "local/institution/o_alice/inbox",
         "ldp.inbox" : "http://localhost:2000/alice/inbox",
         "orchestrator.rules" : "local/orchestrator/alice",
         "slot.id" : "2"
      }
   },

   ...
]
```

```
POST /
```

```
$ curl -X POST 'http://localhost:9190/' | json_pp 
{
   "id" : "e7b1b8ef-6ebf-32ee-3fb3-f261fdfdde04",
   "variables" : {
      "ldp.inbox" : "http://localhost:2000/alice/inbox",
      "orchestrator.inbox" : "local/institution/o_alice/inbox",
      "ldp.events" : "http://localhost:2000/alice/events",
      "orchestrator.rules" : "local/orchestrator/alice",
      "ldn.inbox" : "http://localhost:2000/alice/inbox",
      "slot.id" : "3"
   },
   "comments" : "",
   "stopped" : 49,
   "disabled" : 0,
   "running" : 0,
   "version" : 1,
   "name" : "Copy of Copy of Copy of Orchestrator - http://localhost:2000/alice"
}
```

## /{id} - Get orchestrator

```
GET /{id}
```

```
$ curl http://localhost:9190/e7b1b8ef-6ebf-32ee-73e0-0fe97b4a0e20 | json_pp
{
   "id" : "e7b1b8ef-6ebf-32ee-73e0-0fe97b4a0e20",
   "name" : "Copy of Copy of Orchestrator - http://localhost:2000/alice",
   "comments" : "",
   "version" : 0,
   "stopped" : 49,
   "disabled" : 0,
   "running" : 0,
   "variables" : {
      "orchestrator.rules" : "local/orchestrator/alice",
      "slot.id" : "2",
      "ldp.events" : "http://localhost:2000/alice/events",
      "orchestrator.inbox" : "local/institution/o_alice/inbox",
      "ldn.inbox" : "http://localhost:2000/alice/inbox",
      "ldp.inbox" : "http://localhost:2000/alice/inbox"
   }
}
```

## /{id}/status - Get/set run status orchestrator

```
GET /{id}/status
```

```
$ curl http://localhost:9190/e7b1b8ef-6ebf-32ee-73e0-0fe97b4a0e20/status | json_pp
{
   "status" : "STOPPED"
}
```

```
POST /{id}/status?status=RUNNING|STOPPED
```

```
$ curl -X POST -H 'Content-Type: application/json' --data '{"status":"RUNNING"}' 'http://localhost:9190/e7b1b8ef-6ebf-32ee-73e0-0fe97b4a0e20/status?status=RUNNING' 
{"status":"ok","message":"process RUNNING"}
```

## /{id}/variables - Get/set orchestrator variables

```
GET /{id}/variables
```

```
$ curl -X GET http://localhost:9190/e7b1b8ef-6ebf-32ee-c76a-e6868d01d320/variables | json_pp
{
   "slot.id" : "1",
   "ldp.inbox" : "http://localhost:2000/alice/inbox",
   "ldn.inbox" : "http://localhost:2000/alice/inbox",
   "orchestrator.inbox" : "local/institution/o_alice/inbox",
   "ldp.events" : "http://localhost:2000/alice/events",
   "orchestrator.rules" : "local/orchestrator/alice"
}
```

```
POST /{id}/variables
```

```
$ curl -X POST -H "Content-Type: application/json" --data @body.json 'http://localhost:9190/e7b1b8ef-6ebf-32ee-73e0-0fe97b4a0e20/variables' 
{"status":"ok","message":"variables updated"}
```

where `body.json` like

```
{
   "orchestrator.rules" : "local/orchestrator/alice",
   "ldp.events" : "http://localhost:2000/alice/events",
   "ldp.inbox" : "http://localhost:2000/alice/inbox",
   "ldn.inbox" : "http://localhost:2000/alice/inbox",
   "orchestrator.inbox" : "local/institution/o_alice/inbox",
   "demo.var" : "test123"
}
