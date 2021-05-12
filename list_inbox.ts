import * as fs from "fs";


if (process.argv.length != 4) {
    console.log(`usage: ${process.argv[1]} id inbox`);
    process.exit(1);
}

let id    = process.argv[2];
let inbox = process.argv[3];

fs.access(inbox, fs.constants.R_OK, (err) => {
    if (err) {
      console.log(`${inbox} doesn't exist`);
      process.exit(2);
    }
} );

let container = {};

container['@context'] =  "http://www.w3.org/ns/ldp";
container['@id'] = id;
container['contains'] = [];

fs.readdirSync(inbox).forEach( (f,_) => {
    container['contains'].push(`${id}/${f}`);
});

console.log(JSON.stringify(container));
