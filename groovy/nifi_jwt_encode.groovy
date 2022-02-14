/*
    Encode JWT data 
    
    Required:
        jwt.public - Path public key file
        jwt.private - Path private key file
        jwt.payload - Payload data

    See also: bin/make_pem.sh
*/  

@Grab(group='com.auth0', module='java-jwt', version='3.18.3')

import java.io.File
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.X509EncodedKeySpec
import java.security.spec.PKCS8EncodedKeySpec
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.JWT
import groovy.json.JsonSlurper

def getPublicKey(fileName) {
    def str = new File(fileName).getText('UTF-8')
    return getPublicKeyFromString(str.trim())
}

def getPrivateKey(fileName) {
    def str = new File(fileName).getText('UTF-8')
    return getPrivateKeyFromString(str.trim())
}

def getPublicKeyFromString(key) {
    def publicKeyPEM = key.replace("-----BEGIN PUBLIC KEY-----\n", "")
                          .replace("-----END PUBLIC KEY-----", "")
                          .trim()
    def encoded = publicKeyPEM.decodeBase64()
    def kf = KeyFactory.getInstance("EC")
    def pubKey = kf.generatePublic(
        new X509EncodedKeySpec(encoded)
    ) 
    return pubKey
}

def getPrivateKeyFromString(key) {
    def privateKeyPEM = key.replace("-----BEGIN PRIVATE KEY-----\n", "")
                           .replace("-----END PRIVATE KEY-----", "")
                           .trim()
    def encoded = privateKeyPEM.decodeBase64()
    def kf = KeyFactory.getInstance("EC")
    def privKey = kf.generatePrivate(
        new PKCS8EncodedKeySpec(encoded)
    ) 
    return privKey
}

def flowFile = session.get()

if (!flowFile) return

outputRelation = REL_SUCCESS

try {
    def publicFile  = flowFile.getAttribute("jwt.public")
    def privateFile = flowFile.getAttribute("jwt.private") 
    def payloadJson = flowFile.getAttribute("jwt.payload")

    def payload   = new JsonSlurper().parseText(payloadJson)
    def pubKey    = getPublicKey(publicFile)
    def privKey   = getPrivateKey(privateFile)

    def algorithm = Algorithm.ECDSA256(pubKey,privKey)
    def token     = JWT.create()
                       .withPayload(payload)
                       .sign(algorithm)

    def headerJson = '{"typ":"JWT","alg":"ES256"}'
    
    flowFile = session.putAttribute(flowFile, "jwt.header", headerJson)
    flowFile = session.putAttribute(flowFile, "jwt.token", token)
}
catch(e) {
    outputRelation = REL_FAILURE
    flowFile = session.putAttribute(flowFile, "error", "PROCESS_ERROR")
    flowFile = session.putAttribute(flowFile, "errorMessage", e.getMessage())
}

session.transfer(flowFile, outputRelation)