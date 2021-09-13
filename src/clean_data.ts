import * as fs from "fs";

if (process.argv.length < 3) {
    console.log(`usage: ${process.argv[1]} node [keep]`);
    process.exit(1);
}

let node     = process.argv[2];
let keepLast = process.argv[3] ? 
                    parseInt(process.argv[3]) : null;

clearAllSync(node, keepLast);

function isDirectory(path: string) : boolean {
    try {
        fs.statSync(path);
        return true;
    }
    catch (e) {
        return false;
    }   
}

function clearAllSync(node :string, keepLast? :number) : void {
    let directory = `local/${node}`;

    if (isDirectory(directory)) {
        // We are ok
     
    }
    else {
        console.error(`no such directory - ${directory}`);
        process.exit(2);
    }
    
    fs.readdirSync(directory).forEach( (node,_) => {
        if (keepLast) {
            cleanDirectoryKeepSync(`${directory}/${node}/inbox`,keepLast);
            cleanDirectoryKeepSync(`${directory}/${node}/events`,keepLast);
        }
        else {
            cleanDirectorySync(`${directory}/${node}/inbox`);
            cleanDirectorySync(`${directory}/${node}/events`);
        }
    });
}

function cleanDirectorySync(dir: string) : void {
    if (! isDirectory(dir)) {
        return;
    }
    fs.readdirSync(dir).forEach( (f,_) => {
        if (! f.startsWith(".")) {
            console.log(`[-] ${dir}/${f}`);
            fs.unlinkSync(`${dir}/${f}`);
        }
    });
}

function cleanDirectoryKeepSync(dir: string, keepLast: number) : void {
    if (! isDirectory(dir)) {
        return;
    }
    let compareDate = function (f1, f2) {
        let m1 = fs.statSync(`${dir}/${f1}`).mtime;
        let m2 = fs.statSync(`${dir}/${f2}`).mtime;
    
        if (m1 < m2) { return 1 }
        if (m1 > m2) { return -1 }
    
        return 0;
    }

    let files : string[] = [];

    fs.readdirSync(dir).sort(compareDate).forEach( (f,_) => {
        if (! f.startsWith(".")) {
            files.push(`${dir}/${f}`);
        }
    });

    files.slice(keepLast).forEach( f => {
        console.log(`[-] ${f}`);
        fs.unlinkSync(f);
    });
}