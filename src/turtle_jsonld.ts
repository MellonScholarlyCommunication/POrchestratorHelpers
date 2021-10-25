import fs from 'fs';
import * as N3 from 'n3';
import * as jsonld from 'jsonld';
import { json } from 'stream-consumers';
import { Command } from 'commander';

// Copied from AlexeyMz/ramp-shapeds-perf src/jsonld.ts
jsonld.registerRDFParser('text/turtle', input => {
    return new Promise((resolve, reject) => {
      const quads: N3.Quad[] = [];
      new N3.Parser().parse(input, (error, quad, hash) => {
        if (error) {
          reject(error);
        } else if (quad) {
          quads.push(quad);
        } else {
          resolve(quads as jsonld.Quad[]);
        }
      });
    });
});

const program  = new Command();

program.argument('<file>')
       .argument('[frame]')
       .action( (file,frame,options,command) => {
           quadsToJson(
                file === "-" ? '/dev/stdin' : file, 'text/turtle', frame).then((result) => {
                console.log(result);
            });
       });

program.parse();

async function quadsToJson(inputFile: string, format: string, frameFile: string) {
    const data  = fs.readFileSync(inputFile,'utf-8');
   
    const ld = await jsonld.fromRDF(data,{ format: format});

    if (typeof frameFile !== 'undefined') {
        const frameJson = fs.readFileSync(frameFile, 'utf-8');
        const frameData = JSON.parse(frameJson);
        const json = await jsonld.frame(ld,frameData);        
        return JSON.stringify(json,null,2);
    }
    else {
        const json = await jsonld.flatten(ld);
        return JSON.stringify(json,null,2);
    }
}