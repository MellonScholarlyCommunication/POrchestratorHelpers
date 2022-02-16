/*
 Generate a JWK key

    Required:
        jwt.public - Path public key file
        jwt.private - Path private key fil

    See also: bin/make_pem.sh
*/

@Grab(group='com.nimbusds', module='nimbus-jose-jwt', version='9.19')

import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import com.nimbusds.jose.*
import com.nimbusds.jose.jwk.*

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

    def publicKey  = getPublicKey(publicFile)
    def privateKey = getPrivateKey(privateFile) 

    def jwk = new ECKey.Builder(Curve.P_256,publicKey)
                   .privateKey(privateKey)
                   .keyUse(KeyUse.SIGNATURE)
                   .algorithm(JWSAlgorithm.ES256) 
                   .build()

    def text = "{\"keys\":[" + jwk + "]}" 
    flowFile = session.write(flowFile , { outputStream ->
        outputStream.write(text.getBytes(StandardCharsets.UTF_8))
    } as OutputStreamCallback)
}
catch(e) {
    outputRelation = REL_FAILURE
    flowFile = session.putAttribute(flowFile, "error", "PROCESS_ERROR")
    flowFile = session.putAttribute(flowFile, "errorMessage", e.getMessage())
}

session.transfer(flowFile, outputRelation)