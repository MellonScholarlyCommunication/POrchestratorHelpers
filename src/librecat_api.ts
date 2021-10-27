import fetch from 'node-fetch';
import fs from 'fs';
import *  as N3 from 'n3';
import ns from './Namespaces'
import { Command } from 'commander';

const newEngine = require('@comunica/actor-init-sparql-file').newEngine;
const myEngine  = newEngine();

const librecat_baseurl = 'http://demo.librecat.org/api/v1';
const librecat_front   = 'http://demo.librecat.org';
const librecat_api_key = process.env.LIBRECAT_API_KEY;

if ( ! librecat_api_key) {
    console.error("No LIBRECAT_API_KEY set");
    process.exit(2);
}

const program  = new Command();

program
       .option('-m, --max <num>', 'maximum size of the list')
       .option('-d, --demo', 'demo mode');

program.command('get id')
       .action( (id) => {
            cmd_get(id);
       });

program.command('post file')
       .action( (file) => {
           cmd_post(file);
       });

program.command('delete id') 
       .action( (id) => {
            cmd_delete(id);
       });

program.command('list') 
       .action( () => {
            cmd_list();
       });

program.command('qae url webid [rdf]')
       .action( (url,webid,rdf) => {
            cmd_qae(url,webid,rdf);
       });

program.parse(process.argv);

async function cmd_list() {
    let page;

    const writer   = new N3.Writer({
        prefixes: {
            ldp: ns.ldp(''),
            dcterms: ns.dct(''),
        }
    });

    const { DataFactory } = N3;
    const { namedNode , literal } = DataFactory;

    writer.addQuad(
        namedNode(`${librecat_front}/record/`) ,
        namedNode(ns.rdf('a')),
        namedNode(ns.ldp('BasicContainer'))
    ); 

    writer.addQuad(
        namedNode(`${librecat_front}/record/`) ,
        namedNode(ns.rdf('a')),
        namedNode(ns.ldp('Container'))
    ); 

    const artefacts = await librecat_walker();
    const compare = (a,b) => {
        if (a.date_updated < b.date_updated) {
            return 1;
        }
        if (a.date_updated > b.date_updated) {
            return -1;
        }
        return 0;
    };

    let list ;

    if (program.opts().max) {
        let max = parseInt(program.opts().max);
        if (artefacts.length > max) {
            list = artefacts.sort(compare).slice(0,max);
        }
    }
    else {
        list = artefacts.sort(compare);
    }

    list.forEach( (item) => {
            writer.addQuad(
                namedNode(`${librecat_front}/record/`) ,
                namedNode(ns.ldp('contains')),
                namedNode(`${librecat_front}/record/${item.id}`)
           ); 

           writer.addQuad(
                namedNode(`${librecat_front}/record/${item.id}`),
                namedNode(ns.rdf('type')),
                namedNode(ns.ldp('Resource'))
           );

           writer.addQuad(
                namedNode(`${librecat_front}/record/${item.id}`),
                namedNode(ns.dct('modified')),
                literal(item.date_updated,namedNode(ns.xsd('dateTime')))
            );
    });

    writer.end ( (error,result) => {
        console.log(result);
    });
}

async function librecat_walker() {
    let page: string;
    let artefacts = [];

    do {
        const jdata = typeof page == 'undefined' ? 
                    await api_get(`/publication`) :
                    await api_get(`/publication${page}`);

        if ("errors" in jdata) {
            console.log(JSON.stringify(jdata));
            break;
        }

        const data: any[]  = jdata.data;
        const next: string = jdata.links.next;

        data.forEach( (item) => {
            artefacts.push( { id : item.id , date_updated : item.attributes.date_updated });
        });

        // Calculate the next page
        if (typeof next !== 'undefined') {
            page = next.substring(librecat_baseurl.length + "/publication".length) ;
        }
        else {
            page = undefined;
        }
    } while (typeof page !== 'undefined');

    return artefacts;
}

async function cmd_get(id: string) {
    const jdata = await api_get(`/publication/${id}`);

    console.log(JSON.stringify(jdata));
}

async function pimp_json(json:any, rdfFile: string) {
    const binding = await sparqlQuery(rdfFile, `
        PREFIX foaf: <http://xmlns.com/foaf/0.1/> 
        PREFIX dcterms: <http://purl.org/dc/terms/> 
        PREFIX bibo: <http://purl.org/ontology/bibo/> 

        SELECT ?title ?firstName  ?lastName  ?fullName ?abstract {
            OPTIONAL { ?s dcterms:title ?title } .
            OPTIONAL { ?s foaf:name ?fullName } .
            OPTIONAL { ?s foaf:givenname ?firstName } .
            OPTIONAL { ?s foaf:surname ?lastName } . 
            OPTIONAL { ?s bibo:abstract ?abstract } .
        }
    `);

    const title     = maybeValue(binding[0],'?title');
    const fullName  = maybeValue(binding[0],'?fullName');
    const firstName = maybeValue(binding[0],'?firstName');
    const lastName  = maybeValue(binding[0],'?lastName');
    const abstract  = maybeValue(binding[0],'?abstract');

    if (title) {
        json.data.attributes.title = title;
    }

    if (fullName) {
        json.data.attributes.author[0].full_name = fullName;
    }

    if (firstName) {
        json.data.attributes.author[0].first_name = firstName;
    }

    if (lastName) {
        json.data.attributes.author[0].last_name = lastName;
    }

    if (abstract) {
        json.data.attributes.abstract = [ { text: abstract , lang: 'eng' } ];
    }

    return json;
}

async function sparqlQuery(source, query) {
    const result = await myEngine.query(
                            query, { 
                            sources: [source]
                   });

    const bindings = await result.bindings();

    return bindings;
}

function maybeValue(binding, key) {
    if (binding.has(key)) {
        return binding.get(key).value;
    } 
    else {
        return undefined;
    }
}

async function cmd_qae(url: string, webid: string, rdfFile: string) {
    let jdata = {
        "data" : {
            "attributes" : {
                "author": [
                    { 
                      "first_name" : "John" ,
                      "last_name" : "Doe" ,
                      "full_name" : "Joen Doe"
                    }  
                ] ,
                "main_file_link" : [
                    {
                        "open_access": "1" ,
                        "url" : url
                    }
                ],
                "message" : `Notification from ${webid}` ,
                "oa": "1",
                "status" : "private" ,
                "title" : `Quick and easy for - ${url}`,
                "type" : "preprint",
                "user_id" : 1234 
            } ,
            "type" : "publication"
        }
    };

    if (typeof rdfFile !== undefined 
        && fs.existsSync(rdfFile) && is_readable(rdfFile)) {
        jdata = await pimp_json(jdata, rdfFile);
    }

    if (program.opts().demo) {
        console.log("Demo mode");
        console.log("-------------------------------");
        console.log(JSON.stringify(jdata));
        return;
    }

    try {
        const rdata = await api_post('/publication',jdata);
        console.log(JSON.stringify(rdata));
    }
    catch( e ) {
        console.error('Oops');
        console.error(e);
        process.exit(2);
    }
}

async function cmd_post(path: string) {
    if (! fs.existsSync(path)) {
        console.error(`no such file ${path}`);
        process.exit(2);
    }

    try {
        const data = await fs.readFileSync(path);
        const jdata = JSON.parse(data.toString());

        const rdata = await api_post('/publication',jdata);

        console.log(JSON.stringify(rdata));
    }
    catch( e ) {
        console.error('Oops');
        console.error(e);
        process.exit(2);
    }
}

async function cmd_delete(id: string) {
    const jdata = api_delete(`/publication/${id}`);

    console.log(JSON.stringify(jdata));
}

async function api_get(path: string) {
    const response = await fetch(`${librecat_baseurl}${path}`, {
            method: 'get' ,
            headers: {
                'Content-Type': `application/vnd.api+json` ,
                'Authorization': `Bearer ${librecat_api_key}`
            }
        });
    const jdata = await response.json();
    return jdata;
}

async function api_post(path: string, data: any) {
    const response = await fetch(`${librecat_baseurl}${path}`, {
            method: 'post' ,
            body: JSON.stringify(data),
            headers: {
                'Content-Type': `application/vnd.api+json` ,
                'Authorization': `Bearer ${librecat_api_key}`
            }
    });
    const jdata = await response.json();
    return jdata;
}

async function api_delete(path: string) {
    const response = await fetch(`${librecat_baseurl}${path}`, {
            method: 'delete' ,
            headers: {
                'Content-Type': `application/vnd.api+json` ,
                'Authorization': `Bearer ${librecat_api_key}`
            }
        });
    const jdata = await response.json();
    return jdata;
}

function is_readable(path: string): boolean {
    try {
        fs.accessSync(path, fs.constants.R_OK);
        return true;
    }
    catch (err) {
        return false;
    }
}