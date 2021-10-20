import fetch from 'node-fetch';
import *  as N3 from 'n3';
import ns from './Namespaces';

if (process.argv.length != 3) {
    console.log(`usage: ${process.argv[1]} personId`);
    process.exit(1)
}

const baseUrl = 'https://biblio.ugent.be';

let personId = process.argv[2];

biblioLdp(personId);

async function biblioLdp(personId: string) {
    const writer   = new N3.Writer({
        prefixes: {
            ldp: ns.ldp(''),
            dcterms: ns.dct(''),
        }
    });
    const { DataFactory } = N3;
    const { namedNode , literal } = DataFactory

    writer.addQuad(
        namedNode('') ,
        namedNode(ns.rdf('a')),
        namedNode(ns.ldp('BasicContainer'))
    ); 

    writer.addQuad(
        namedNode('') ,
        namedNode(ns.rdf('a')),
        namedNode(ns.ldp('Container'))
    ); 

    try {
        const response = await fetch(`${baseUrl}/person/${personId}/publication/export?format=json`);
        const body = await response.text();

        body.split(/\r?\n/).forEach(line => {
            if (line) {
               const data     = JSON.parse(line);
               const id       = data._id;
               const modified = data.date_updated.replace(/ /,"T").replace(/$/,"Z");

               writer.addQuad(
                    namedNode('') ,
                    namedNode(ns.ldp('contains')),
                    namedNode(`${baseUrl}/publication/${id}`)
               ); 

               writer.addQuad(
                    namedNode(`${baseUrl}/publication/${id}`),
                    namedNode(ns.rdf('type')),
                    namedNode(ns.ldp('Resource'))
               );

               writer.addQuad(
                    namedNode(`${baseUrl}/publication/${id}`),
                    namedNode(ns.dct('modified')),
                    literal(modified,namedNode(ns.xsd('dateTime')))
               );
            }
        });
    }
    catch (err) {
        console.error(err);
    }

    writer.end ( (error,result) => {
        console.log(result);
    });
}