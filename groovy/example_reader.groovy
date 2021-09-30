#!/usr/bin/env groovy

@Grab(group='commons-io', module='commons-io', version='2.8.0')
@Grab(group='org.apache.jena', module='jena-core', version='3.13.1')
@Grab(group='org.apache.jena', module='jena-tdb', version='3.13.1')
@Grab(group='org.slf4j', module='slf4j-api', version='1.7.26')
@Grab(group='org.slf4j', module='slf4j-simple', version='1.7.26')
@Grab(group='com.github.jsonld-java', module='jsonld-java', version='0.9.0')

import org.apache.jena.rdf.model.Model
import org.apache.jena.util.FileUtils
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.riot.RDFFormat
import java.io.FileInputStream

def loadModel(fileName, type, baseUrl="urn:dummy") {
    Model model = ModelFactory.createDefaultModel()
    model.read(new FileInputStream(fileName),baseUrl,type)
    return model;
}

def baseUrl

def cli = new CliBuilder()

cli.with {
    b(longOpt: 'baseUrl', 'Set baseUrl', args: 1, required: false)
}
def options = cli.parse(args)

if (options && options.b) {
    baseUrl = options.b
}

if (options.arguments().size() < 1) {
   System.err.println("usage: example_reader.groovy [-b baseUrl] dataFile [outputType]")
   System.exit(1)
}

def dataFile   = options.arguments()[0]
def inputType  = "TURTLE"
def outputType = "TURTLE"

if (options.arguments().size() > 1) {
    outputType = options.arguments()[1]
}

if (dataFile.matches('.*\\.ttl$')) {
    inputType = "TURTLE"
}
else if (dataFile.matches('.*\\.nt$')) {
    inputType = "NTRIPLES"
}
else if (dataFile.matches('.*\\.jsonld$')) {
    inputType = "JSONLD"
}
else if (dataFile.matches('.*\\.n3$')) {
    inputType = "N3"
}
else if (dataFile.matches('.\\.rdf$')) {
    inputType = "RDFJSON"
}

outputFormat = null;

if (outputType == 'TURTLE') {
    outputFormat = RDFFormat.TURTLE
}
else if (outputType == 'NTRIPLES') {
    outputFormat = RDFFormat.NTRIPLES
}
else if (outputType == 'RDFXML') {
    outputFormat = RDFFormat.RDFXML
}
else if (outputType == 'NT') {
    outputFormat = RDFFormat.NT
}
else if (outputType == 'NQ') {
    outputFormat = RDFFormat.NQ
}
else if (outputType == 'TRIG') {
    outputFormat = RDFFormat.NQ
}
else if (outputType == 'JSONLD') {
    outputFormat = RDFFormat.JSONLD
}
else if (outputType == 'JSONLD_COMPACT_FLAT') {
    outputFormat = RDFFormat.JSONLD_COMPACT_FLAT
}
else if (outputType == 'JSONLD_COMPACT_PRETTY') {
    outputFormat = RDFFormat.JSONLD_COMPACT_PRETTY
}
else if (outputType == 'JSONLD_COMPACT_FLAT') {
    outputFormat = RDFFormat.JSONLD_COMPACT_FLAT
}
else if (outputType == 'JSONLD_FLATTEN_PRETTY') {
    outputFormat = RDFFormat.JSONLD_FLATTEN_PRETTY
}
else if (outputType == 'JSONLD_FLATTEN_FLAT') {
    outputFormat = RDFFormat.JSONLD_FLATTEN_FLAT
}
else if (outputType == 'JSONLD_FRAME_PRETTY') {
    outputFormat = RDFFormat.JSONLD_FRAME_PRETTY
}
else if (outputType == 'JSONLD_FRAME_FLAT') {
    outputFormat = RDFFormat.JSONLD_FRAME_FLAT
}
else if (outputType == 'JSONLD_PRETTY') {
    outputFormat = RDFFormat.JSONLD_PRETTY
}
else if (outputType == 'JSONLD_FLAT') {
    outputFormat = RDFFormat.JSONLD_FLAT
}
else {
    System.err.println("Unknown format")
    System.exit(2)
}

dataModel = loadModel(dataFile,inputType,baseUrl)

//RDFDataMgr.write(System.out, dataModel, RDFFormat.JSONLD_FLATTEN_PRETTY);
RDFDataMgr.write(System.out, dataModel, outputFormat);
