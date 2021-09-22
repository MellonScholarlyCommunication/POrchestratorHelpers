import *  as N3 from 'n3';
import { IQueryResultBindings } from "@comunica/actor-init-sparql";
import { newEngine } from "@comunica/actor-init-sparql";
import ns from './Namespaces'
import { write } from 'fs';

const mySource = 'http://localhost:2000';
const myEngine = newEngine();
const writer   = new N3.Writer({
    prefixes: {
        foaf: ns.foaf(''),
        schema: ns.schema('')
    }
});
const { DataFactory } = N3;
const { namedNode }   = DataFactory

listInboxes(mySource).then ( ids => {
    writer.addQuad(
        namedNode('#me'),
        namedNode(ns.rdf('a')),
        namedNode(ns.foaf('Agent'))
    );
    writer.addQuad(
        namedNode('#me'),
        namedNode(ns.rdf('a')),
        namedNode(ns.schema('Service'))
    );
    ids.forEach( id => {
        writer.addQuad(
            namedNode('#me'),
            namedNode(ns.foaf('knows')),
            namedNode(`${id}/card.ttl#me`)
        );
    });
    writer.end ( (error,result) => {
        console.log(result);
    });
});

async function listInboxes(source: string) : Promise<string[]> {
    const boxes = await queryBinding(source,`
        SELECT ?box WHERE {
            ?ldp <http://www.w3.org/ns/ldp#contains> ?box .
            ?box a <http://www.w3.org/ns/ldp#Container> .
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