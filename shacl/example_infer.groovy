@Grab(group='org.topbraid', module='shacl', version='1.3.2')
@Grab(group='commons-io', module='commons-io', version='2.8.0')
@Grab(group='org.apache.jena', module='jena-core', version='3.13.1')

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

dataModel  = loadModel("data/record.ttl", "TURTLE");
shapesModel = loadModel("shapes/shape.ttl", "TURTLE");

// Perform the rule calculation, using the data model
// also as the rule model - you may have them separated
Model result = RuleUtil.executeRules(dataModel, shapesModel, null, null);

result.write(System.out, FileUtils.langTurtle);
