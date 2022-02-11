/*
 Decode POST parameters sent in a flowFile payload
*/
import java.net.URLDecoder;

def flowFile = session.get()

if (!flowFile) return

def outputRelation = REL_SUCCESS

try {
    def postParam = [:]

    session.read(flowFile , { inputStream ->
        def paramStr = inputStream.getText()

        if (paramStr) {
            def parts    = paramStr.split("&")

            for (String s: parts) {
                def nv    = s.split("=",2);
                def name  = URLDecoder.decode(nv[0],"UTF-8")
                def value = URLDecoder.decode(nv[1],"UTF-8")

                postParam[name] = value
            }
        }
    } as InputStreamCallback)

    for (p in postParam) {
        flowFile = session.putAttribute(flowFile, "http.post.param." + p.key, p.value)
    }
}
catch(e) {
    outputRelation = REL_FAILURE
    flowFile = session.putAttribute(flowFile, "error", "PROCESS_ERROR")
    flowFile = session.putAttribute(flowFile, "errorMessage", e.getMessage())
}

session.transfer(flowFile, outputRelation)