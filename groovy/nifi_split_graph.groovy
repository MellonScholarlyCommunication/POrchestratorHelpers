/*

Split a flow file based on a graph pattern

Required:
  - Flow file with TURTLE, RDF/XML, JSON-LD,...
  - Attribute `subject` with a subject IRI
  - Attribute `predicate` with a predicate IRI

Optional:
  - Attribute `inputType` with the format of the Flow file (TURTLE,N-TRIPLES,RDF/XML, JSON-LD)
     - See https://jena.apache.org/documentation/io/rdf-input.html
  - Attribute `inputSource` with "flowfile" or an attribute containing the graph
  - Attribite `outputType` with the name of a serialization
  - Attribute `baseUrl` with a baseUrl for the flow document

Output:
  - For search ?subject ?preficate pattern a new flow file with the fragment graph
  - At error an attribute `error` with values:
      - VALIDATION_ERROR : when the input is not valid
      - PROCESS_ERROR : when the flow file or shape file can't be processed for some reason
      - An `errorMessage` with the reason
*/
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
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.ArrayList
import java.util.List

def loadModel(inputStream, type, baseUrl="urn:x:base") {
    Model model = ModelFactory.createDefaultModel()
    model.read(inputStream,baseUrl,type)
    return model;
}

def string2format(format)  {
    if (!format) {
        return null
    }
    if (format == 'TURTLE') {
        return RDFFormat.TURTLE
    }
    else if (format == 'NTRIPLES') {
        return RDFFormat.NTRIPLES
    }
    else if (format == 'RDFXML') {
        return RDFFormat.RDFXML
    }
    else if (format == 'NT') {
        return RDFFormat.NT
    }
    else if (format == 'NQ') {
        return RDFFormat.NQ
    }
    else if (format == 'TRIG') {
        return RDFFormat.NQ
    }
    else if (format == 'JSON') {
        return RDFFormat.JSONLD_COMPACT_PRETTY
    }
    else if (format == 'JSON-LD') {
        return RDFFormat.JSONLD
    }
    else if (format == 'JSONLD') {
        return RDFFormat.JSONLD
    }
    else if (format == 'JSONLD_COMPACT_FLAT') {
        return RDFFormat.JSONLD_COMPACT_FLAT
    }
    else if (format == 'JSONLD_COMPACT_PRETTY') {
        return RDFFormat.JSONLD_COMPACT_PRETTY
    }
    else if (format == 'JSONLD_COMPACT_FLAT') {
        return RDFFormat.JSONLD_COMPACT_FLAT
    }
    else if (format == 'JSONLD_FLATTEN_PRETTY') {
        return RDFFormat.JSONLD_FLATTEN_PRETTY
    }
    else if (format == 'JSONLD_FLATTEN_FLAT') {
        return RDFFormat.JSONLD_FLATTEN_FLAT
    }
    else if (format == 'JSONLD_FRAME_PRETTY') {
        return RDFFormat.JSONLD_FRAME_PRETTY
    }
    else if (format == 'JSONLD_FRAME_FLAT') {
        return RDFFormat.JSONLD_FRAME_FLAT
    }
    else if (format == 'JSONLD_PRETTY') {
        return RDFFormat.JSONLD_PRETTY
    }
    else if (format == 'JSONLD_FLAT') {
        return RDFFormat.JSONLD_FLAT
    }
    else {
        return null;
    }
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

flowFile = session.get()

if (!flowFile) return

outputRelation = REL_SUCCESS
defaultInputType = "TURTLE"
defaultInputSource = "flowfile"
defaultBaseUrl = "urn:x:base"

def splitFlowFiles = new ArrayList<>();

try {
    // Read the id attribute
    def subjectIri = flowFile.getAttribute("subject")

    if (!subjectIri) {
        return
    }

    def predicateIri = flowFile.getAttribute("predicate")

    if (!predicateIri) {
        return
    }

    // Read the inputType attribute
    def inputType = flowFile.getAttribute("inputType")

    if (!inputType) {
        inputType = defaultInputType
    }

    // Read the inputSource attribute
    def inputSource = flowFile.getAttribute("inputSource")

    if (!inputSource) {
        inputSource = defaultInputSource
    }

    // Read the outputType attribute
    def outputType = flowFile.getAttribute("outputType")

    if (!outputType) {
        outputType = inputType
    }

    // Read the baseUrl attribute 
    def baseUrl = flowFile.getAttribute("baseUrl")

    if (!baseUrl) {
        baseUrl = defaultBaseUrl
    }

    def dataModel = null;

    if (inputSource == "flowfile" ) {
        session.read(flowFile , { inputStream ->
            dataModel = loadModel(inputStream, inputType, baseUrl)
        } as InputStreamCallback)
    }
    else {
        def graph = flowFile.getAttribute(inputSource)
        def bt = new ByteArrayInputStream(graph.getBytes())
        dataModel = loadModel(bt, inputType, baseUrl)
        bt.close()
    }

    def subject   = dataModel.getResource(subjectIri)
    def predicate = dataModel.getProperty(predicateIri)
    def it        = dataModel.listStatements(subject,predicate,null)

    while (it.hasNext()) {
        def st          = it.nextStatement();
        def object      = st.getObject();
        def accumulator = ModelFactory.createDefaultModel()

        followResource(object.asResource(), accumulator)

        def clone = session.clone(flowFile)

        if (inputSource == "flowfile" ) {
            session.write(clone, { outputStream ->
                RDFDataMgr.write(outputStream, accumulator, string2format(outputType));
            } as OutputStreamCallback)
        }
        else {
            def bt = new ByteArrayOutputStream();
            RDFDataMgr.write(bt, accumulator, string2format(outputType));
            clone = session.putAttribute(clone,inputSource,bt.toString("UTF-8"));
        }

        splitFlowFiles.add(clone)
    }
}
catch(e) {
    outputRelation = REL_FAILURE
    flowFile = session.putAttribute(flowFile, "error", "PROCESS_ERROR")
    flowFile = session.putAttribute(flowFile, "errorMessage", e.getMessage())
}

if (outputRelation == REL_SUCCESS) {
    session.remove(flowFile)
    session.transfer(splitFlowFiles, outputRelation)
}
else {
    session.transfer(flowFile, outputRelation)
}
