#!/usr/bin/env groovy

@Grab(group='com.nimbusds', module='nimbus-jose-jwt', version='9.19')

import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import com.nimbusds.jose.*
import com.nimbusds.jose.jwk.*
import groovy.cli.commons.CliBuilder

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

def cli = new CliBuilder()

cli.with {
    pi(longOpt: 'private', 'Set private key', args: 1, required: false);
    pu(longOpt: 'public', 'Set public key', args: 1, required: true);
}

def options = cli.parse(args)

if (!options) {
    System.exit(1)
}

def publicFile  = options.pu
def privateFile = options.pi

def publicKey  = getPublicKey(publicFile)

def jwk = new ECKey.Builder(Curve.P_256,publicKey)

if (privateFile) {
   def privateKey = getPrivateKey(privateFile)  
   jwk = jwk.privateKey(privateKey)
}

def kid = publicKey.getEncoded().digest('SHA-256')

jwk = jwk.keyUse(KeyUse.SIGNATURE)
         .keyID(kid)
         .algorithm(JWSAlgorithm.ES256)
         .build()

println(jwk)