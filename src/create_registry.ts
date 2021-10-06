import *  as N3 from 'n3';
import { IQueryResultBindings } from "@comunica/actor-init-sparql";
import { newEngine } from "@comunica/actor-init-sparql";
import ns from './Namespaces';
import { fstat, write } from 'fs';
import { Command } from 'commander';
import fs from 'fs';

let myWebIdSource;
let myWebIdFile;

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
            myWebIdSource = flags.baseurl ? flags.baseurl : myWebIdSource ;
            myWebIdFile  = file;
       });

program.parse(process.argv);

// Loop over an LDP baseUrl and list all collections
// E.g. we assume that all containers at the baseUrl are Pods with 
// a "well known" webid (card.ttl)..not true in general
if (myWebIdSource) {
    listInboxes(myWebIdSource).then ( ids => {
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
}

if (myWebIdFile) {
    fs.readFileSync(myWebIdFile,'utf-8').split(/\r?\n/).forEach( (line) => {
        // Skip empty lines or outcommented lines
        if (line.length > 0 && line.match(/.+/) && ! line.match(/^\s*#/)) {
            writer.addQuad(
                namedNode('#me'),
                namedNode(ns.foaf('knows')),
                namedNode(line)
            );
        }
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