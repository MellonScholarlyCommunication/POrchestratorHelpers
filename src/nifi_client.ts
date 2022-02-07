import fetch from 'node-fetch';
import { Command } from 'commander';
import * as fs from "fs";

const FormData = require('form-data');

const X_OFFSET  = 400;
const Y_OFFSET  = 200;
const MAX_SLOTS = 100;

const apiUrl  = 'http://localhost:8080/nifi-api';
const program = new Command();

const exitCode = main();

exitCode.then( i => process.exit(i));

async function main() {
    let exitCode = 0;

    program.option('-a,--api','api url')
           .option('-b,--base <base>','base url')
           .option('-r,--root <root>','root process node');

    program.command('list-templates')
        .action( async () => {
            const res = await list_templates();

            if (!res) {
                exitCode = 2;
            }

            console.log(JSON.stringify(res));
        });

    program.command('list-process-groups [id]')
        .action( async (id) => {
            const res = await list_process_groups(id);

            if (!res) {
                exitCode = 3;
            }

            console.log(JSON.stringify(res));
        });

    program.command('count-process-groups [id]')
        .action( async (id) => {
             const res = await count_process_group(id);

             if (!res) {
                 exitCode = 4;
             }

             console.log(res);
        });

    program.command('create-process-group template')
        .action( async (template) => {
            const res = await create_process_group(null,template);

            if (! res) {
                exitCode = 5;
            }

            console.log(JSON.stringify(res));
        });

    program.command('delete-process-group root id')
        .action( async (root,id) => {
            const res = await delete_process_group(root,id);

            if (! res) {
                exitCode = 6;
            }

            console.log(JSON.stringify(res));
        });

    program.command('get-process-group [id]')
        .action( async (id) => {
            const res = await get_process_group(id);

            if (! res) {
                exitCode = 7;
            }

            console.log(JSON.stringify(res));
        });

    program.command('status id') 
        .action( async (id) => {
            const res = await get_status(id);

            if (! res) {
                exitCode = 8;
            }

            console.log(JSON.stringify(res));
        });

    program.command('stop id') 
        .action( async (id) => {
            const res = await startstop_process_group(id,'STOPPED');

            if (! res) {
                exitCode = 9;
            }

            console.log(JSON.stringify(res));
        });

    program.command('start id') 
        .action( async (id) => {
            const res = await startstop_process_group(id,'RUNNING');

            if (! res) {
                exitCode = 10;
            }

            console.log(JSON.stringify(res));
        });

    program.command('get-variables id')
        .action( async (id) => {
            const group = await get_process_group(id);

            if (! group) {
                exitCode = 11;
            }

            console.log(JSON.stringify(group['variables']));
        });

    program.command('set-variables id file') 
        .action( async (id,file) => {
            const fileOrStdin = file === '-' ? '/dev/stdin' : file;
            const jsonData = fs.readFileSync(fileOrStdin, { encoding: 'utf8', flag: 'r'});
            const data = JSON.parse(jsonData);

            // The slot.id is the only key we don't want to see
            delete data['slot.id'];

            const res = await set_variables(id,data);

            if (! res) {
                exitCode = 12;
            }

            console.log(JSON.stringify(res));
        });

    program.command('set-name id name')
        .action( async (id,name) =>{
            const res = await set_name(id,name);

            if (! res) {
                exitCode = 13;
            }
            console.log(JSON.stringify(res));
        });

    await program.parseAsync(process.argv);

    return exitCode;
}

async function list_templates() {
    const base = program.opts().api || apiUrl;

    const response = await fetch(`${base}/resources`);

    if (! response.ok) {
        return null;
    }

    const data = await response.json();

    const templates = data['resources'].filter( res => {
        return res['identifier'].startsWith("/template");
    });

    return { "templates" : templates };
}

async function list_process_groups(id:string = "root") {
    const base  = program.opts().api || apiUrl;
    const xbase = program.opts().base;
    const root  = program.opts().root;

    const parentId = typeof root === 'undefined' ? id : root;

    const response = await fetch(`${base}/process-groups/${parentId}/process-groups`);

    if (! response.ok) {
        console.error(await response.text());
        return [];
    }

    const data = await response.json();

    const groups = data['processGroups'].map( group => {
        const meta = {};

        if (xbase) {
            meta['record'] = `${xbase}/${group['id']}`;
            meta['status'] = `${xbase}/${group['id']}/status`;
            meta['variables'] = `${xbase}/${group['id']}/variables`;
        }

        return {
            id: group['id'] ,
            name: group['component']['name'],
            comments: group['component']['comments'],
            running: group['runningCount'],
            stopped: group['stoppedCount'],
            disabled: group['disabledCount'],
            variables: group['component']['variables'],
            version: group['revision']['version'] ,
            meta: meta
        };
    });

    return groups;
}

async function get_process_group(id:string="root") {
    const groups = await list_process_groups(id);

    if (groups.length == 0) {
        return null;
    }

    return groups.filter( group => group.id === id)[0];
}

async function get_status(id:string="root") {
    const group = await get_process_group(id);

    if (! group) {
        return null;
    }

    if (group['running'] > 0) {
        return { status: 'RUNNING'};
    }
    else {
        return { status: 'STOPPED'};
    }
}

async function startstop_process_group(id:string,state:string) {
    const group = await get_status(id); 

    if (! group) {
        return null;
    }

    const base = program.opts().api || apiUrl; 

    const response = await fetch(`${base}/flow/process-groups/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        } ,
        body: JSON.stringify({
            id: id ,
            state: state
        })
    });

    if (response.ok) {
        return {
            status: "ok" ,
            message: `process ${state}`
        }
    }
    else {
        const message = await response.text();
        return {
            status: "error" ,
            message: message
        }
    }
}

async function set_variables(id:string, variables:any) {
    const base = program.opts().api || apiUrl; 

    const response = await fetch(`${base}/process-groups/${id}/variable-registry`);

    if (! response.ok) {
        return null;
    }

    const data = await response.json();

    const version = data['processGroupRevision']['version'];

    const variableArray = [];

    for (const key in variables) {
        variableArray.push({
            variable: { 
                name: key, 
                value: variables[key]
            }
        });
    };

    const updateRequest = {
        processGroupRevision: {
            version: version
        } ,
        variableRegistry: {
            processGroupId: id,
            variables: variableArray
        }
    };

    const update_response = await fetch(`${base}/process-groups/${id}/variable-registry/update-requests`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        } ,
        body: JSON.stringify(updateRequest)
    });

    if (! update_response.ok ) {
        const message = await update_response.text();
        return {
            status: "error",
            message: message
        }
    }
    else {
        return {
            status: "ok" ,
            message: "variables updated"
        }
    }
}

async function count_process_group(id:string="root") {
    const groups = await list_process_groups(id);

    return groups.length;
}

async function find_free_slot(id:string) {
    const groups = await list_process_groups(id);
    
    if (! groups || groups.length == 0) {
        return 0;
    }

    const slots = new Set<number>();

    for (let i = 0 ; i < MAX_SLOTS ; i++) {
        slots.add(i);
    }

    let isError = false;

    groups.forEach( group => {
        const slot = group['variables']['slot.id'];

        if (typeof slot === 'undefined') {
            isError = true;
        }
        else {
            slots.delete(parseInt(slot));
        }
    });

    if (isError) {
        return null;
    }

    if (slots.size == 0) {
        return null;
    }

    return slots.values().next().value;
}

async function create_process_group(id:string="root",template:string) {
    const root = program.opts().root;

    const parentId = typeof root === 'undefined' ? id : root;

    const nextSlot = await find_free_slot(parentId);

    if (nextSlot === null) {
        return {
            status: "error",
            message: "no more slots available"
        }
    }

    const base = program.opts().apiUrl || apiUrl; 

    const origin_x = Math.trunc( nextSlot % 5 ) * X_OFFSET;
    const origin_y = Math.trunc( nextSlot / 5 ) * Y_OFFSET;

    const response = await fetch(`${base}/process-groups/${parentId}/template-instance`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        } ,
        body: JSON.stringify({
            originX: origin_x ,
            originY: origin_y ,
            templateId: template
        })
    });

    if (! response.ok) {
        const message = await response.text();
        return {
            status: "error",
            message: message
        }
    }

    const data  = await response.json();
    const newid = data['flow']['processGroups'][0]['id'];
    const group = await get_process_group(newid);

    const variables = group['variables'];

    variables['slot.id'] = `${nextSlot}`;

    const res = await set_variables(newid,variables);

    if (res.status === 'error') {
        return {
            status: 'error',
            message: `created ${newid} failed to set slot.id`
        }
    }

    return await get_process_group(newid);
}

async function set_name(id:string,name:string) {
    const base = program.opts().api || apiUrl; 

    const group = await get_process_group(id);

    if (! group) {
        return null;
    }

    const response = await fetch(`${base}/process-groups/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        } ,
        body: JSON.stringify({
            revision: {
                version: group['version']
            },
            component: {
                id: id,
                name: name
            }
        })
    });

    if (! response.ok ) {
        const message = await response.text();
        return {
            status: "error",
            message: message
        }
    }
    else {
        return {
            status: "ok",
            message: "renamed process-group"           
        }
    }
}

async function delete_process_group(root:string,id:string) {
    const base = program.opts().api || apiUrl; 

    const groupList = await list_process_groups(root);

    const deleteGroup = groupList.filter( res => res.id === id);

    if (! deleteGroup || deleteGroup.length == 0) {
        return null;
    }

    const version  = deleteGroup[0]['version'];

    const response = await fetch(`${base}/process-groups/${id}?version=${version}` , {
        method: 'DELETE'
    });

    if (! response.ok ) {
        const message = await response.text();
        return {
            status: "error",
            message: message
        }
    }
    else {
        return {
            status: "ok",
            message: "deleted process-group"           
        }
    }
}