import fetch from 'node-fetch';
import fs from 'fs';
import *  as N3 from 'n3';
import ns from './Namespaces'
import { Command } from 'commander';

const librecat_baseurl = 'http://demo.librecat.org/api/v1';
const librecat_front   = 'http://demo.librecat.org';
const librecat_api_key = process.env.LIBRECAT_API_KEY;

if ( ! librecat_api_key) {
    console.error("No LIBRECAT_API_KEY set");
    process.exit(2);
}

const program  = new Command()

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

    do {
        const jdata = typeof page == 'undefined' ? 
                    await api_get(`/publication`) :
                    await api_get(`/publication${page}`);

        if ("errors" in jdata) {
            console.log(JSON.stringify(jdata));
        }

        const data: any[]  = jdata.data;
        const next: string = jdata.links.next;

        data.forEach( (item) => {

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
                literal(item.attributes.date_updated,namedNode(ns.xsd('dateTime')))
            );

        });

        // Calculate the next page
        if (typeof next !== 'undefined') {
            page = next.substring(librecat_baseurl.length + "/publication".length) ;
        }
        else {
            page = undefined;
        }
    } while (typeof page !== 'undefined');

    writer.end ( (error,result) => {
        console.log(result);
    })
}

async function cmd_get(id: string) {
    const jdata = await api_get(`/publication/${id}`);

    console.log(JSON.stringify(jdata));
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