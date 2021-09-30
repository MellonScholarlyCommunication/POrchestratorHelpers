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

def baseUrl

def cli = new CliBuilder()

cli.with {
    b(longOpt: 'baseUrl', 'Set baseUrl', args: 1, required: false)
}

def options = cli.parse(args)

if (options && options.b) {
    baseUrl = options.b
}

if (options.arguments().size() < 3) {
   System.err.println("usage: example_split.groovy [-b] dataFile subject predicate")
   System.exit(1)
}

def dataFile   = options.arguments()[0]
def subjectIri = options.arguments()[1]
def policyIri  = options.arguments()[2]

def dataModel = loadModel(dataFile,"TURTLE",baseUrl)

def subject   = dataModel.getResource(subjectIri)
def property  = dataModel.getProperty(policyIri)
def it        = dataModel.listStatements(subject,property,null)

System.out.println("----")

while (it.hasNext()) {
    def st = it.nextStatement();
    def object = st.getObject();

    def accumulator = ModelFactory.createDefaultModel()

    followResource(object.asResource(), accumulator)

    RDFDataMgr.write(System.out, accumulator, RDFFormat.TURTLE);

    System.out.println("----")
}

//RDFDataMgr.write(System.out, dataModel, RDFFormat.JSONLD_FLATTEN_PRETTY);
//RDFDataMgr.write(System.out, dataModel, RDFFormat.NTRIPLES);
