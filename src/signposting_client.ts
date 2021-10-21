import fetch from 'node-fetch';
import parse from 'parse-link-header';
import * as htmlparser2 from 'htmlparser2';
import { Command } from 'commander';

var urlParser = require('url-parse');

const program  = new Command();

program.option('-d,--download','download');

program.command('list url')
        .action( (url) => {
            cmd_list(url);
        });

program.command('get url rel') 
        .action( (url,rel) => {
            cmd_get(url,rel);
        });

program.parse(process.argv);

async function cmd_list(url: string) {
    const links = await document_links(url);
    console.log(links);
}

async function cmd_get(url: string, rel: string) {
    const links = await document_links(url);

    if (! (rel in links)) {
        return;
    }

    const rel_url = links[rel]['url'];

    if (program.opts().download) {
        const response = await fetch(rel_url);

        const body = await response.text();

        console.log(body);
    }
    else {
        console.log(rel_url);
    }
}

async function document_links(url: string) {
    const response = await fetch(url);

    const headers = await response.headers;

    const link = headers.get('link');
    const contentType = headers.get('content-type');

    let parsed = {};

    if (link ) {
        parsed = parse(link);
    }

    if (contentType.match(/^text\/html/)) {
        parsed = html_links(response, parsed);
    }

    return parsed;
}

async function html_links(response: Response, parsed: any) {
    const body   = await response.text();
    const origin = urlParser(response.url,true)['origin'];

    const parser = new htmlparser2.Parser({
        onopentag(name, attributes) {
            if (name === 'link') {
                const rel  = attributes['rel'];
                let   url  = attributes['href'];
                if (url.match(/^\//)) {
                    url = origin + url;
                }
                parsed[rel] = { rel: rel , url: url};
            }
        },
        ontext(text) {},
        onclosetag(name) {}
    });

    parser.write(body);

    parser.end();

    return parsed;
}