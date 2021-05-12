@Grab(group='org.topbraid', module='shacl', version='1.3.2')
@Grab(group='commons-io', module='commons-io', version='2.8.0')
@Grab(group='org.apache.jena', module='jena-core', version='3.13.1')
@Grab(group='com.github.jsonld-java', module='jsonld-java', version='0.9.0')

import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.util.FileUtils
import org.topbraid.shacl.validation.ValidationUtil
import org.topbraid.shacl.util.ModelPrinter
import org.apache.jena.rdf.model.Resource
import org.topbraid.jenax.util.JenaDatatypes
import org.topbraid.shacl.vocabulary.SH
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

  // Read the file flowFile
  session.read(flowFile , { inputStream ->
      dataModel = loadModel(inputStream, dataType)
      shapesModel = loadModel(new FileInputStream(shapesFile), shapesType)

      report = ValidationUtil.validateModel(dataModel, shapesModel, true)

      if (report.hasProperty(SH.conforms, JenaDatatypes.FALSE)) {
          outputRelation = REL_FAILURE
          flowFile = session.putAttribute(flowFile, "error", "VALIDATION_ERROR")
      }
  } as InputStreamCallback)
}
catch(e) {
  outputRelation = REL_FAILURE
  flowFile = session.putAttribute(flowFile, "error", "PROCESS_ERROR")
  flowFile = session.putAttribute(flowFile, "errorMessage", e.getMessage())
}

session.transfer(flowFile, outputRelation)
