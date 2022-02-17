/*
  Create a thumbprint from a JWK encoded key

  Requires:

    - jwt.header
 */

@Grab(group='com.nimbusds', module='nimbus-jose-jwt', version='9.19')

import com.nimbusds.jose.*
import com.nimbusds.jose.jwk.*
import groovy.json.JsonSlurper

def flowFile = session.get()

if (!flowFile) return

outputRelation = REL_SUCCESS

try {
    def header = flowFile.getAttribute("jwt.header")
    def json   = new JsonSlurper().parseText(header)
    def jwk    = JWK.parse(json['jwk'])

    def thumbprint = jwk.computeThumbprint().toString()

    flowFile = session.putAttribute(flowFile, "jwt.thumbprint", thumbprint)
}
catch(e) {
    outputRelation = REL_FAILURE
    flowFile = session.putAttribute(flowFile, "error", "PROCESS_ERROR")
    flowFile = session.putAttribute(flowFile, "errorMessage", e.getMessage())
}

session.transfer(flowFile, outputRelation)