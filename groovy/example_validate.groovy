#!/usr/bin/env groovy

@Grab(group='org.topbraid', module='shacl', version='1.3.2')
@Grab(group='commons-io', module='commons-io', version='2.8.0')
@Grab(group='org.apache.jena', module='jena-core', version='3.13.1')
@Grab(group='org.slf4j', module='slf4j-api', version='1.7.30')
@Grab(group='org.slf4j', module='slf4j-simple', version='1.7.30')
@Grab(group='com.github.jsonld-java', module='jsonld-java', version='0.9.0')

import org.apache.jena.rdf.model.Model
import org.apache.jena.util.FileUtils
import org.topbraid.shacl.validation.ValidationUtil
import org.topbraid.jenax.util.JenaDatatypes
import org.topbraid.shacl.vocabulary.SH
import org.apache.jena.rdf.model.ModelFactory
import java.io.FileInputStream;

def loadModel(fileName, type, baseUrl="urn:dummy") {
    Model model = ModelFactory.createDefaultModel()
    model.read(new FileInputStream(fileName), baseUrl,type)
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

if (options.arguments().size() != 2) {
   System.err.println("usage: example_validate.groovy [-b] dataFile shapesFile ")
   System.exit(1)
}

def inputType  = "TURTLE"
def dataFile   = options.arguments()[0]
def shapesFile = options.arguments()[1]

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

def dataModel  = loadModel(dataFile, inputType, baseUrl);
def shapesModel = loadModel(shapesFile,"TURTLE", baseUrl);

def report = ValidationUtil.validateModel(dataModel, shapesModel, true);

report.getModel().write(System.out, FileUtils.langTurtle);

if(report.hasProperty(SH.conforms, JenaDatatypes.FALSE)) {
		// See https://github.com/TopQuadrant/shacl/issues/56
		System.exit(1);
}
