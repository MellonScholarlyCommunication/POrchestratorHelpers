#!/usr/bin/env groovy

@Grab(group='commons-io', module='commons-io', version='2.8.0')
@Grab(group='org.apache.jena', module='jena-core', version='3.13.1')
@Grab(group='org.apache.jena', module='jena-tdb', version='3.13.1')
@Grab(group='org.apache.jena', module='jena-text', version='3.13.1')
@Grab(group='org.slf4j', module='slf4j-api', version='1.7.30')
@Grab(group='org.slf4j', module='slf4j-simple', version='1.7.30')
@Grab(group='com.github.jsonld-java', module='jsonld-java', version='0.9.0')

import org.apache.jena.rdf.model.Model
import org.apache.jena.util.FileUtils
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.riot.RDFFormat
import org.apache.jena.query.QueryFactory
import org.apache.jena.query.QueryExecutionFactory
import java.io.FileInputStream

def loadModel(fileName, type) {
    Model model = ModelFactory.createDefaultModel()
    model.read(new FileInputStream(fileName),"urn:dummy",type)
    return model;
}

if (args.size() != 2) {
   System.err.println("usage: example_sparql.groovy dataFile sparqlFile ")
   System.exit(1)
}

inputType  = "TURTLE"

if (args[0].matches('.*\\.ttl$')) {
    inputType = "TURLTLE"
}
else if (args[0].matches('.*\\.nt$')) {
    inputType = "NTRIPLES"
}
else if (args[0].matches('.*\\.jsonld$')) {
    inputType = "JSONLD"
}
else if (args[0].matches('.*\\.n3$')) {
    inputType = "N3"
}
else if (args[0].matches('.\\.rdf$')) {
    inputType = "RDFJSON"
}

dataModel  = loadModel(args[0], inputType)

query = QueryFactory.create(new File(args[1]).getText("UTF-8"))

qexec = QueryExecutionFactory.create( query, dataModel )
results = qexec.execConstruct()

RDFDataMgr.write(System.out, results, RDFFormat.NQUADS);
