/*
Decode DPoP header into a JWT attribute
*/

@Grab(group='com.auth0', module='java-jwt', version='3.18.3')

import com.auth0.jwt.*
import java.util.Base64
import java.nio.charset.StandardCharsets

def flowFile = session.get()

if (!flowFile) return

outputRelation = REL_SUCCESS

try {
    def dpop = flowFile.getAttribute("http.headers.DPoP")

    def jwt = JWT.decode(dpop)

    def headerJson = new String(
                    Base64.getUrlDecoder().decode(
                        jwt.getHeader()
                    ), StandardCharsets.UTF_8)

    def payloadJson = new String(
                    Base64.getUrlDecoder().decode(
                        jwt.getPayload()
                    ), StandardCharsets.UTF_8) 
                    
    flowFile = session.putAttribute(flowFile, "jwt.header", headerJson)
    flowFile = session.putAttribute(flowFile, "jwt.payload", payloadJson)
}
catch(e) {
    outputRelation = REL_FAILURE
    flowFile = session.putAttribute(flowFile, "error", "PROCESS_ERROR")
    flowFile = session.putAttribute(flowFile, "errorMessage", e.getMessage())
}

session.transfer(flowFile, outputRelation)