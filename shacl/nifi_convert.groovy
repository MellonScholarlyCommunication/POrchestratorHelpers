/*

Convert a Flow file from one format to another

Required:
  - Flow file with TURTLE, RDF/XML, JSON-LD,...
  - Attribute `inputType` with the input type
  - Attribute `outputType` with the output type


Output:
  - An updated flow with in the (new) format
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
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.riot.RDFFormat

def loadModel(inputStream, type) {
    Model model = ModelFactory.createDefaultModel()
    model.read(inputStream,"urn:x:base",type)
    return model;
}

flowFile = session.get()

if (!flowFile) return

outputRelation = REL_SUCCESS
defaultInputType = "TURTLE"
defaultOutputType = "TURTLE"

try {
    // Read the inputType attribute
    inputType = flowFile.getAttribute("inputType")

    if (!inputType) {
        inputType = defaultInputType
    }

    // Read the outputType attribute
    outputType = flowFile.getAttribute("outputType")

    if (!outputType) {
        outputType = defaultOutputType
    }

    outputFormat = null;

    if (outputType == 'TURTLE') {
        outputFormat = RDFFormat.TURTLE
    }
    else if (outputType == 'NTRIPLES') {
        outputFormat = RDFFormat.NTRIPLES
    }
    else if (outputType == 'RDFXML') {
        outputFormat = RDFFormat.RDFXML
    }
    else if (outputType == 'NT') {
        outputFormat = RDFFormat.NT
    }
    else if (outputType == 'NQ') {
        outputFormat = RDFFormat.NQ
    }
    else if (outputType == 'TRIG') {
        outputFormat = RDFFormat.NQ
    }
    else if (outputType == 'JSONLD') {
        outputFormat = RDFFormat.JSONLD
    }
    else if (outputType == 'JSONLD_COMPACT_FLAT') {
        outputFormat = RDFFormat.JSONLD_COMPACT_FLAT
    }
    else if (outputType == 'JSONLD_COMPACT_PRETTY') {
        outputFormat = RDFFormat.JSONLD_COMPACT_PRETTY
    }
    else if (outputType == 'JSONLD_COMPACT_FLAT') {
        outputFormat = RDFFormat.JSONLD_COMPACT_FLAT
    }
    else if (outputType == 'JSONLD_FLATTEN_PRETTY') {
        outputFormat = RDFFormat.JSONLD_FLATTEN_PRETTY
    }
    else if (outputType == 'JSONLD_FLATTEN_FLAT') {
        outputFormat = RDFFormat.JSONLD_FLATTEN_FLAT
    }
    else if (outputType == 'JSONLD_FRAME_PRETTY') {
        outputFormat = RDFFormat.JSONLD_FRAME_PRETTY
    }
    else if (outputType == 'JSONLD_FRAME_FLAT') {
        outputFormat = RDFFormat.JSONLD_FRAME_FLAT
    }
    else if (outputType == 'JSONLD_PRETTY') {
        outputFormat = RDFFormat.JSONLD_PRETTY
    }
    else if (outputType == 'JSONLD_FLAT') {
        outputFormat = RDFFormat.JSONLD_FLAT
    }

    // Read/Write the file flowFile
    session.write(flowFile , { inputStream , outputStream ->
        dataModel = loadModel(inputStream, inputType)

        RDFDataMgr.write(outputStream, dataModel, outputFormat);
    } as StreamCallback)
}
catch(e) {
    outputRelation = REL_FAILURE
    flowFile = session.putAttribute(flowFile, "error", "PROCESS_ERROR")
    flowFile = session.putAttribute(flowFile, "errorMessage", e.getMessage())
}

session.transfer(flowFile, outputRelation)
