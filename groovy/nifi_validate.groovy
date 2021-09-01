/*

Validate the contents of a Flow file.

Required:
  - Flow file with TURTLE, RDF/XML, JSON-LD,...
  - Attribute `shapesFile` with the location of a SHACL shapes file

Optional:
  - Attribute `dataType` with the format of the Flow file (TURTLE,N-TRIPLES,RDF/XML, JSON-LD)
     - See https://jena.apache.org/documentation/io/rdf-input.html
  - Attribute `shapesType` with the format of the Flow file (TURTLE,N-TRIPLES,RDF/XML, JSON-LD)

Output:
  - Flow file as-is
  - At error an attribute `error` with values:
      - VALIDATION_ERROR : when the input is not valid
      - PROCESS_ERROR : when the flow file or shape file can't be processed for some reason
      - An `errorMessage` with the reason
*/
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
import java.io.ByteArrayOutputStream

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

          stream = new ByteArrayOutputStream()
          report.write(stream, FileUtils.langTurtle)
          stream.close()

          flowFile = session.putAttribute(flowFile, "errorMessage", new String(stream.toByteArray()))
      }
  } as InputStreamCallback)
}
catch(e) {
  outputRelation = REL_FAILURE
  flowFile = session.putAttribute(flowFile, "error", "PROCESS_ERROR")
  flowFile = session.putAttribute(flowFile, "errorMessage", e.getMessage())
}

session.transfer(flowFile, outputRelation)
