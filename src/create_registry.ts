/**
 * Transform a local list of Webids into a foaf:knows turtle document.
 * See: make registry
 */
import *  as N3 from 'n3';
import { IQueryResultBindings } from "@comunica/actor-init-sparql";
import { newEngine } from "@comunica/actor-init-sparql";
import ns from './Namespaces';
import { fstat, write } from 'fs';
import { Command } from 'commander';
import fs from 'fs';

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
       .action( (file) => {
            myWebIdFile  = file;
       });

program.parse(process.argv);

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

writer.end ( (error,result) => {
    console.log(result);
});