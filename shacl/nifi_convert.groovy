@Grab(group='org.topbraid', module='shacl', version='1.3.2')
@Grab(group='commons-io', module='commons-io', version='2.8.0')
@Grab(group='org.apache.jena', module='jena-core', version='3.13.1')
@Grab(group='com.github.jsonld-java', module='jsonld-java', version='0.9.0')

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
    else if (outputType == 'N3') {
        outputFormat = RDFFormat.N3
    }
    else if (outputType == 'JSONLD') {
        outputFormat = RDFFormat.JSONLD
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
