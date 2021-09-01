import * as fs from "fs";
import * as http from "http";

var Url = require('url-parse');

if (process.argv.length != 4) {
    console.error(`usage: ${process.argv[1]} inbox file`);
    process.exit(1);
}

let inbox = process.argv[2];
let file  = process.argv[3];

fs.access(file, fs.constants.R_OK, (err) => {
    if (err) {
      console.error(`${file} doesn't exist`);
      process.exit(2);
    }
} );

const notification = fs.readFileSync(file, 'utf-8');

const url = new Url(inbox);

const req = http.request(
    {
      host: url.host,
      port: url.port ? url.port : 80,
      path: url.pathname,
      method: 'POST',
      headers: {
        'Content-Type': 'application/ld+json'
      }
    },
    response => {
        console.log(response.statusCode + ' : ' + response.statusMessage);
        console.log(response.headers);

        if (response.statusCode == 200 || response.statusCode == 202) {
            process.exit(0);
        }
        else {
            process.exit(2);
        }
    }
);

req.write(notification);

req.end();
