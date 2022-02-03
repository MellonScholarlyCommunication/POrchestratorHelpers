# POrchestrator API

## / - List orchestrators

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

## /{id}/status - Running status orchestrator

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
$ curl -X POST 'http://localhost:9190/e7b1b8ef-6ebf-32ee-73e0-0fe97b4a0e20/status?status=RUNNING' 
{"status":"ok","message":"process RUNNING"}
```

