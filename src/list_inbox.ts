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

let compareDate = function (f1, f2) {
    let fd1 = fs.openSync(`${inbox}/${f1}`,'r');
    let fd2 = fs.openSync(`${inbox}/${f2}`,'r');

    let m1 = fs.fstatSync(fd1).mtime;
    let m2 = fs.fstatSync(fd2).mtime;

    fs.closeSync(fd1);
    fs.closeSync(fd2);

    if (m1 < m2) { return 1 }
    if (m1 > m2) { return -1 }

    return 0;
}

fs.readdirSync(inbox).sort(compareDate).forEach( (f,_) => {
    container['contains'].push(`${id}/${f}`);
});

console.log(JSON.stringify(container));
