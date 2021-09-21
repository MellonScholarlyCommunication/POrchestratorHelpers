import { IQueryResultBindings } from "@comunica/actor-init-sparql";
import { newEngine } from "@comunica/actor-init-sparql";

interface ICard {
    id:     string;
    type:   string;  
    name:   string;
    inbox:  string;
    orchestrator?: string;
}

const mySource = 'http://localhost:2000';
const myEngine = newEngine();

cardReader(mySource).then( cards => {
    console.log(cards);
});

function maybeValue(binding, key: string) : string {
    if (binding.has(key)) {
        return binding.get(key).value;
    } 
    else {
        return undefined;
    }
}

async function cardReader(source: string) : Promise<ICard[]> {
    const boxes = await listInboxes(source);

    const cards = boxes.map( item => readCard(item) );

    return Promise.all(cards);
}

async function readCard(url: string) : Promise<ICard> {
    const wellKnownCard = `${url}/card.ttl`;

    const binding = await queryBinding(wellKnownCard, `
        PREFIX as: <http://www.w3.org/ns/activitystreams#> 
        PREFIX ex: <https://www.example.org/>
        SELECT ?id ?type ?name ?inbox ?orchestrator
        WHERE {
            { ?id a ?type .
              ?id as:name ?name .
              ?id as:inbox ?inbox
            } OPTIONAL 
            {
              ?id ex:orchestrator ?orchestrator .
            }
        }
    `);

    if (binding.length != 1) {
        return undefined;
    }

    const result: ICard = {
        id:    maybeValue(binding[0],'?id'),
        type:  maybeValue(binding[0],'?type'),
        name:  maybeValue(binding[0],'?name'),
        inbox: maybeValue(binding[0],'?inbox'),
        orchestrator: maybeValue(binding[0],'?orchestrator')
    }

    return result;
}

async function listInboxes(source: string) : Promise<string[]> {
    const boxes = await queryBinding(source,`
        SELECT ?box WHERE {
            ?ldp <http://www.w3.org/ns/ldp#contains> ?box
        }
    `);

    return new Promise( (resolve) => {
        let ids = boxes.map( item => item.get('?box').value );
        resolve(ids);
    });
}

async function queryBinding(source: string, query: string) {
    const result = await myEngine.query(
                            query, { 
                            sources: [source]
                   });

    const bd = await (result as IQueryResultBindings).bindings();
    return bd;
}