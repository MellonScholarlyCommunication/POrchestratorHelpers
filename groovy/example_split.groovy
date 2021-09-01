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

def loadModel(fileName, type) {
    Model model = ModelFactory.createDefaultModel()
    model.read(new FileInputStream(fileName),"urn:dummy",type)
    return model;
}

def followResource(resource, model) {

    def it = resource.listProperties()

    while (it.hasNext()) {
        def st = it.nextStatement()

        model.add(st)

        def v  = st.getObject()

        if (v.isResource()) {
             followResource(v.asResource(), model)
        }
    }

    it.close()
}

if (args.size() < 3) {
   System.err.println("usage: example_split.groovy dataFile subject predicate")
   System.exit(1)
}

dataModel = loadModel(args[0],"TURTLE")

subjectIri = args[1]
policyIri  = args[2]

subject   = dataModel.getResource(subjectIri)
property  = dataModel.getProperty(policyIri)
it        = dataModel.listStatements(subject,property,null)

System.out.println("----")

while (it.hasNext()) {
    st = it.nextStatement();
    object = st.getObject();

    def accumulator = ModelFactory.createDefaultModel()

    followResource(object.asResource(), accumulator)

    RDFDataMgr.write(System.out, accumulator, RDFFormat.TURTLE);

    System.out.println("----")
}


//RDFDataMgr.write(System.out, dataModel, RDFFormat.JSONLD_FLATTEN_PRETTY);
//RDFDataMgr.write(System.out, dataModel, RDFFormat.NTRIPLES);
