import fetch from 'node-fetch';
import * as htmlparser2 from 'htmlparser2';
import { Command } from 'commander';

var urlParser = require('url-parse');
var LinkHeader = require('http-link-header');

const program  = new Command();

program
        .option('-d,--download','download')
        .option('-H,--header <Str>','header');

program.command('list url')
        .action( async (url) => {
            process.exitCode = await cmd_list(url);
        });

program.command('get url rel [type]') 
        .action( async (url,rel,type) => {
            process.exitCode = await cmd_get(url,rel,type);
        });

program.parse(process.argv);

async function cmd_list(url: string) {
    const links = await document_links(url);
    console.log(links);
    return 0;
}

async function cmd_get(url: string, rel: string, type: string) {
    const links = await document_links(url);

    let result = links.filter( item =>  item['rel'] === rel );

    if (type !== undefined ) {
        result = links.filter( item => item['type'] && item['type'] === type);
    }

    if (! result) {
        console.error(`No ${rel} relator found`);
        return 2;
    }

    const rel_url = result.pop()['uri'];

    const opts = program.opts();

    if (opts.download) {
        const headers = {};

        if (opts.header) {
            const name_value = opts.header.split(/\s*:\s*/,2);
            headers[name_value[0]] = name_value[1];
        }

        const response = await fetch(rel_url, {
            headers: headers
        });

        const body = await response.text();

        console.log(body);
    }
    else {
        console.log(rel_url);
    }

    return 0;
}

async function document_links(url: string) {
    const response = await fetch(url);

    const headers = await response.headers;

    const link = headers.get('link');
    const contentType = headers.get('content-type');

    let parsed = [];

    if (link) {
        parsed = LinkHeader.parse(link).refs;
    }

    if (contentType.match(/^text\/html/)) {
        parsed = await html_links(response, parsed);
    }

    return rel2abs(response,parsed);
}

function rel2abs(response: Response, parsed: any[]) {
    const origin = urlParser(response.url,true)['origin'];

    return parsed.map( (item) => {
        let url = item['uri'];
        if (url.match(/^\//)) {
            item['uri'] = origin + url;
        }
        else if (!url.match(/^http/)) {
            item['uri'] = origin + '/' + url;
        }
        return item;
    });
}

async function html_links(response: Response, parsed: any) {
    const body   = await response.text();

    const parser = new htmlparser2.Parser({
        onopentag(name, attributes) {
            if (name === 'link') {
                const rel  = attributes['rel'];
                const url  = attributes['href'];
                const type = attributes['type'];
                let doc = { rel: rel , uri: url };
                if (type) {
                    doc['type'] = type;
                }
                parsed.push(doc);
            }
        },
        ontext(text) {},
        onclosetag(name) {}
    });

    parser.write(body);

    parser.end();

    return parsed;
}