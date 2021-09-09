import * as fs from "fs";

if (process.argv.length != 4) {
    console.log(`usage: ${process.argv[1]} id collection`);
    process.exit(1);
}

let id    = process.argv[2];
let collection = process.argv[3];

id = id.replace(/\/$/,"");

fs.access(collection, fs.constants.R_OK, (err) => {
    if (err) {
      console.log(`${collection} doesn't exist`);
      process.exit(2);
    }
} );

let container = {};

container['@context'] =  "http://www.w3.org/ns/ldp";
container['@id'] = id;
container['contains'] = [];

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
        container['contains'].push(`${id}/${f}`);
    }
});

console.log(JSON.stringify(container));
