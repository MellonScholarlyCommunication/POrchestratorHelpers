/*
  Convert a flat JSON payload into attributes
*/
import org.apache.commons.io.IOUtils
import java.nio.charset.*

def flowFile = session.get();

if (flowFile == null) {
    return;
}

def slurper = new groovy.json.JsonSlurper()
def attrs = [:] as Map<String,String>

session.read(flowFile,
    { inputStream ->
        def text = IOUtils.toString(inputStream, StandardCharsets.UTF_8)
        def obj = slurper.parseText(text)
        obj.each {k,v ->
           attrs[k] = v.toString()
        }
    } as InputStreamCallback)

flowFile = session.putAllAttributes(flowFile, attrs)

session.transfer(flowFile, REL_SUCCESS)
