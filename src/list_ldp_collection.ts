/**
 * Transforms a directory listing to a LDP Container
 * Use by Nifi Solid mock server
 */
import * as fs from "fs";

if (process.argv.length != 4) {
    console.log(`usage: ${process.argv[1]} id collection`);
    process.exit(1);
}

let id         = process.argv[2];
let collection = process.argv[3];

id = id.replace(/\/$/,"");

let modified : string;

try {
    let stat    = fs.statSync(collection);
    modified    =  new Date(stat.mtime).toISOString();
}
catch(err) {
    console.log(`${collection} doesn't exist`);
    process.exit(2);
}

let container = {};

container['@context'] =  [
    "http://www.w3.org/ns/ldp" ,
    { "modified": {
      "@id": "http://purl.org/dc/terms/modified",
      "@type": "http://www.w3.org/2001/XMLSchema#dateTime"
      }
    }
];
container['id'] = id;
container['type'] = [ 'BasicContainer' , 'Container' ];
container['contains'] = [];
container['modified'] = modified;

let compareDate = function (f1, f2) {
    let m1 = fs.statSync(`${collection}/${f1}`).mtime;
    let m2 = fs.statSync(`${collection}/${f2}`).mtime;

    if (m1 < m2) { return 1 }
    if (m1 > m2) { return -1 }

    return 0;
}

fs.readdirSync(collection).sort(compareDate).forEach( (f,_) => {
    if (f.startsWith('.')) {
        // ignore
    }
    else {
        let stat = fs.statSync(`${collection}/${f}`);
        let type = stat.isDirectory() ? 
                    [ "BasicContainer" , "Container" , "Resource"] :
                    [ "Resource"];
        let modified = new Date(stat.mtime).toISOString();
        let resource = {
            "id" : `${id}/${f}` ,
            "type": type ,
            "modified": modified
        };
        container['contains'].push(resource);
    }
});

console.log(JSON.stringify(container));
