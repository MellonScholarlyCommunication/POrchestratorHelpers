import *  as N3 from 'n3';
import { IQueryResultBindings } from "@comunica/actor-init-sparql";
import { newEngine } from "@comunica/actor-init-sparql";
import ns from './Namespaces';
import { fstat, write } from 'fs';
import { Command } from 'commander';
import fs from 'fs';

let mySource = 'http://localhost:2000';
let myExtra  = undefined;

const myEngine = newEngine();
const writer   = new N3.Writer({
    prefixes: {
        foaf: ns.foaf(''),
        schema: ns.schema('')
    }
});
const { DataFactory } = N3;
const { namedNode }   = DataFactory

const program  = new Command();

program.command('parse [file]')
       .option('-b, --baseurl <url>','baseurl of the demo pod')
       .action( (file,flags) => {
            mySource = flags.baseurl ? flags.baseurl : mySource ;
            myExtra  = file;
       });

program.parse(process.argv);

listInboxes(mySource).then ( ids => {
    writer.addQuad(
        namedNode('#me'),
        namedNode(ns.rdf('type')),
        namedNode(ns.foaf('Agent'))
    );
    writer.addQuad(
        namedNode('#me'),
        namedNode(ns.rdf('type')),
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

if (myExtra) {
    fs.readFileSync(myExtra,'utf-8').split(/\r?\n/).forEach( (line) => {
        writer.addQuad(
            namedNode('#me'),
            namedNode(ns.foaf('knows')),
            namedNode(line)
        );
    });
}

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