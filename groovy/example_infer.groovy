#!/usr/bin/env groovy

@Grab(group='org.topbraid', module='shacl', version='1.3.2')
@Grab(group='commons-io', module='commons-io', version='2.8.0')
@Grab(group='org.apache.jena', module='jena-core', version='3.13.1')
@Grab(group='org.slf4j', module='slf4j-api', version='1.7.30')
@Grab(group='org.slf4j', module='slf4j-simple', version='1.7.30')
@Grab(group='com.github.jsonld-java', module='jsonld-java', version='0.9.0')
@Grab(group='com.github.jsonld-java', module='jsonld-java', version='0.9.0')

import org.apache.jena.rdf.model.Model
import org.apache.jena.util.FileUtils
import org.topbraid.jenax.util.JenaUtil
import org.topbraid.shacl.rules.RuleUtil
import org.topbraid.shacl.util.ModelPrinter
import org.apache.jena.rdf.model.ModelFactory
import java.io.FileInputStream;

def loadModel(fileName, type) {
    Model model = ModelFactory.createDefaultModel()
    model.read(new FileInputStream(fileName),"urn:dummy",type)
    return model;
}

if (args.size() != 2) {
   System.err.println("usage: example_infer.groovy dataFile shapesFile ")
   System.exit(1)
}

inputType  = "TURTLE"

if (args[0].matches('.*\\.ttl$')) {
    inputType = "TURTLE"
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

dataModel  = loadModel(args[0], inputType);
shapesModel = loadModel(args[1], "TURTLE");

// Perform the rule calculation, using the data model
// also as the rule model - you may have them separated
Model result = RuleUtil.executeRules(dataModel, shapesModel, null, null);

result.write(System.out, FileUtils.langTurtle);
