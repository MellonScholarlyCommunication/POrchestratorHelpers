/*

Use SHACL rule expression to infer new triples from the input data

Required:
  - Flow file with TURTLE, RDF/XML, JSON-LD,...
  - Attribute `shapesFile` with the location of a SHACL shapes file

Optional:
  - Attribute `dataType` with the format of the Flow file (TURTLE,N-TRIPLES,RDF/XML, JSON-LD)
     - See https://jena.apache.org/documentation/io/rdf-input.html
  - Attribute `shapesType` with the format of the Flow file (TURTLE,N-TRIPLES,RDF/XML, JSON-LD)
  - Attribute `outputDestination` =  attribute | flowfile (default flowfile)
  - Attribute `baseUrl` with a baseUrl for the flow document

Output:
  - An updated flowfile with the result of the inference or a new attribute output
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
import org.topbraid.jenax.util.JenaUtil
import org.topbraid.shacl.rules.RuleUtil
import org.topbraid.shacl.util.ModelPrinter
import java.io.FileInputStream
import java.io.ByteArrayOutputStream

def loadModel(inputStream,type,baseUrl="urn:x:base") {
    Model model = ModelFactory.createDefaultModel()
    model.read(inputStream,baseUrl,type)
    return model;
}

flowFile = session.get()

if (!flowFile) return

outputRelation = REL_SUCCESS
defaultInputType = "TURTLE"
defaultOutputDestination = "flowfile"
defaultBaseUrl = "urn:x:base"

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

  // Read the outputDestination attribute
  outputDestination = flowFile.getAttribute("outputDestination")

  if (!outputDestination) {
      outputDestination = defaultOutputDestination
  }

  // Read the baseUrl attribute
  baseUrl = flowFile.getAttribute("baseUrl")

  if (!baseUrl) {
      baseUrl = defaultBaseUrl
  }

  results = null

  // Read/Write the file flowFile
  session.read(flowFile , { inputStream ->
      dataModel = loadModel(inputStream, dataType, baseUrl)

      shapesModel = loadModel(new FileInputStream(shapesFile), shapesType, baseUrl)

      results = RuleUtil.executeRules(dataModel, shapesModel, null, null)
  } as InputStreamCallback)

  if (outputDestination == "flowfile") {
      session.write(flowFile, { outputStream ->
          results.write(outputStream, FileUtils.langTurtle)
      } as OutputStreamCallback)
  }
  else if (outputDestination == "attribute") {
      bArr = new ByteArrayOutputStream()
      results.write(bArr, FileUtils.langTurtle)

      flowFile = session.putAttribute(flowFile, "output", bArr.toString("UTF-8"))
  }
  else {
      // Do nothing
  }
}
catch(e) {
  outputRelation = REL_FAILURE
  flowFile = session.putAttribute(flowFile, "error", "PROCESS_ERROR")
  flowFile = session.putAttribute(flowFile, "errorMessage", e.getMessage())
}

session.transfer(flowFile, outputRelation)
