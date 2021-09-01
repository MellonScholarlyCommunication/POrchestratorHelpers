// Change the DSpace metadata with one field.
// Overwrite existing values
//
// Usage: node $0  dc.relation.hasversion http://my.site.org/review/1234

import * as fs from "fs";

if (process.argv.length != 4) {
    console.log(`usage: ${process.argv[1]} field value`);
    process.exit(1);
}

let field_name   = process.argv[2];
let field_value  = process.argv[3];

const json = fs.readFileSync('/dev/stdin', 'utf-8');
const data = JSON.parse(json);

const parts = field_name.split(".",3);

if (parts.length < 2) {
    console.log(`field needs a least two parts (e.g. dc.title)`);
    process.exit(2);
}

// Remove the previous version
// Remove the expanded field names
const result = data.filter( x  => {
    if (x.key === field_name) {
        return false;
    }
    else {
        return true;
    }
}).map( x => {
    delete x['element'];
    delete x['qualifier'];
    delete x['schema'];
    return x;
});

result.push(
  {
      'key'       : field_name ,
      'value'     : field_value ,
      'language'  : null
  }
);

console.log(JSON.stringify(result));
