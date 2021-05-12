@Grab(group='org.topbraid', module='shacl', version='1.3.2')
@Grab(group='commons-io', module='commons-io', version='2.8.0')
@Grab(group='org.apache.jena', module='jena-core', version='3.13.1')
@Grab(group='com.github.jsonld-java', module='jsonld-java', version='0.9.0')

import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.util.FileUtils
import org.topbraid.jenax.util.JenaUtil
import org.topbraid.shacl.rules.RuleUtil
import org.topbraid.shacl.util.ModelPrinter
import java.io.FileInputStream

def loadModel(inputStream, type) {
    Model model = ModelFactory.createDefaultModel()
    model.read(inputStream,"urn:x:base",type)
    return model;
}

flowFile = session.get()

if (!flowFile) return

outputRelation = REL_SUCCESS
defaultInputType = "TURTLE"

try {
  // Read the shapesFile attribute
  shapesFile = flowFile.getAttribute("shapesFile")

  if (!shapesFile) return

  // Read the dataType attribute
  dataType = flowFile.getAttribute("dataType")

  if (!dataType) {
      dataType = defaultInputType
  }

  // Read the shapesType attribute
  shapesType = flowFile.getAttribute("shapesType")

  if (!shapesType) {
      shapesType = defaultInputType
  }

  // Read/Write the file flowFile
  session.write(flowFile , { inputStream , outputStream ->
      dataModel = loadModel(inputStream, dataType)

      shapesModel = loadModel(new FileInputStream(shapesFile), shapesType)

      results = RuleUtil.executeRules(dataModel, shapesModel, null, null)

      results.write(outputStream, FileUtils.langTurtle)
  } as StreamCallback)
}
catch(e) {
  outputRelation = REL_FAILURE
  flowFile = session.putAttribute(flowFile, "error", "PROCESS_ERROR")
  flowFile = session.putAttribute(flowFile, "errorMessage", e.getMessage())
}

session.transfer(flowFile, outputRelation)
