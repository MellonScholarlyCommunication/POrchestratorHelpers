import * as mimetype from 'mimetype';

if (process.argv.length != 3) {
    console.log(`usage: ${process.argv[1]} node [keep]`);
    process.exit(1);
}

let filename = process.argv[2];

mimetype.set('.n3', 'application/n3');
mimetype.set('.ttl', 'text/turtle');
mimetype.set('.jsonld', 'application/ld+json');

// We set the default to JSON-LD in this demo..
// Any files without extension are probably that
console.log(mimetype.lookup(filename) || 'application/ld+json');