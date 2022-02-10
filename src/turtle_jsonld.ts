/**
 * Command to convert Turtle files into JSON-LD
 */
import fs from 'fs';
import * as N3 from 'n3';
import * as jsonld from 'jsonld';
import { Command } from 'commander';
import * as Mustache from 'mustache';

const program  = new Command();

program.argument('<file>')
       .argument('[frame]')
       .action( (file,frame,options,command) => {
           quadsToJson(
                file === "-" ? '/dev/stdin' : file, 'text/turtle', frame).then((result) => {
                console.log(result);
            });
       });

program.parse(process.argv);

async function quadsToJson(inputFile: string, format: string, frameFile: string) {
    const data  = fs.readFileSync(inputFile,'utf-8');
   
    jsonld.registerRDFParser(format, input => N3Parser(input));

    const ld = await jsonld.fromRDF(data,{format: format});

    if (typeof frameFile !== 'undefined') {
        const subject   = await getSubject(data);
        let frameJson = fs.readFileSync(frameFile, 'utf-8');

        frameJson = Mustache.render(frameJson, { id: subject });

        const frameData = JSON.parse(frameJson);
        const json = await jsonld.frame(ld,frameData);        
        return JSON.stringify(json,null,2);
    }
    else {
        const json = await jsonld.flatten(ld);
        return JSON.stringify(json,null,2);
    }
}

async function N3Parser(input: string) : Promise<N3.Quad[]> {
    return new Promise((resolve, reject) => {
      const quads: N3.Quad[] = [];
      new N3.Parser().parse(input, (error, quad, _) => {
        if (error) {
          reject(error);
        } else if (quad) {
          quads.push(quad);
        } else {
          resolve(quads as jsonld.Quad[]);
        }
      });
    });
}

async function getSubject(input: string) : Promise<string | null> {
      const seen = new Set();
   
      const quads = await N3Parser(input);

      quads.forEach( quad => {
          seen.add( quad.subject.value );
      });

      quads.forEach( quad => {
          seen.delete( quad.object.value );
      });

      if (seen.size == 1) {
          return seen.values().next().value;
      }

      return null;
}