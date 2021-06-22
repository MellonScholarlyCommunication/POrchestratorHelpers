/*

Execute a SPARQL query in input data

Required:
  - Flow file with TURTLE, RDF/XML, JSON-LD,...
  - Attribute `sparqlQuery` with the SPARQL query

Optional:
  - Attribute `dataType` with the format of the Flow file (TURTLE,N-TRIPLES,RDF/XML, JSON-LD)
     - See https://jena.apache.org/documentation/io/rdf-input.html
  - Attribute `outputDestination` =  attribute | flowfile (default flowfile)
  - Attribute `outputAttribute` = name of output attribute (default output)

Output:
  - An updated flowfile with the result of the inference or a new attribute output
  - At error an attribute `error` with values:
      - VALIDATION_ERROR : when the input is not valid
      - PROCESS_ERROR : when the flow file or shape file can't be processed for some reason
      - An `errorMessage` with the reason
*/
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
import org.apache.jena.query.Query
import org.apache.jena.query.QueryFactory
import org.apache.jena.query.QueryExecutionFactory
import org.apache.jena.query.ResultSetFormatter
import java.io.PrintWriter

def loadModel(inputStream, type) {
    Model model = ModelFactory.createDefaultModel()
    model.read(inputStream,"urn:x:base",type)
    return model;
}

def executeQuery(sparlQuery, dataModel) {
    query = QueryFactory.create(sparlQuery)
    qexec = QueryExecutionFactory.create( query, dataModel )

    outputString = null

    if ( Query.QueryTypeConstruct == qexec.getQuery().getQueryType() ) {
        ByteArrayOutputStream bArr = new ByteArrayOutputStream()
        results = qexec.execConstruct()
        RDFDataMgr.write(bArr, results, RDFFormat.NQUADS)
        outputString = bArr.toString()
    }
    else if (Query.QueryTypeSelect == qexec.getQuery().getQueryType() ) {
        ByteArrayOutputStream bArr = new ByteArrayOutputStream()
        results = qexec.execSelect()
        ResultSetFormatter.outputAsTSV(bArr, results)
        outputString = bArr.toString()
    }
    else if (Query.QueryTypeDescribe == qexec.getQuery().getQueryType() ) {
        ByteArrayOutputStream bArr = new ByteArrayOutputStream()
        results = qexec.execDescribe()
        RDFDataMgr.write(bArr, results, RDFFormat.NQUADS)
        outputString = bArr.toString()
    }
    else if (Query.QueryTypeAsk == qexec.getQuery().getQueryType() ) {
        results = qexec.execAsk()
        outputString = results ? "true" : "false"
    }
    else {
        return null
    }

    return outputString
}

flowFile = session.get()

if (!flowFile) return

outputRelation = REL_SUCCESS
defaultInputType = "TURTLE"
defaultOutputDestination = "flowfile"
defaultOutputAttribute = "output"

try {
  // Read the SPARQL query attribute
  sparlQuery = flowFile.getAttribute("sparqlQuery")

  if (!sparlQuery) {
      return
  }

  // Read the dataType attribute
  dataType = flowFile.getAttribute("dataType")

  if (!dataType) {
      dataType = defaultInputType
  }

  // Read the outputDestination attribute
  outputDestination = flowFile.getAttribute("outputDestination")

  if (!outputDestination) {
      outputDestination = defaultOutputDestination
  }

  // Read the outputAttribute
  outputAttribute = flowFile.getAttribute("outputAttribute")

  if (!outputAttribute) {
      outputAttribute = defaultOutputAttribute
  }

  results = null

  // Read/Write the file flowFile
  session.read(flowFile , { inputStream ->
      dataModel = loadModel(inputStream, dataType)

      results = executeQuery(sparlQuery, dataModel)
  } as InputStreamCallback)

  if (outputDestination == "flowfile") {
      session.write(flowFile, { outputStream ->
          writer = new PrintWriter(outputStream)
          writer.print(results)
          writer.flush()
          writer.close()
      } as OutputStreamCallback)
  }
  else if (outputDestination == "attribute") {
      flowFile = session.putAttribute(flowFile, outputAttribute, results)
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
