#!/usr/bin/env groovy

@Grab(group='com.auth0', module='java-jwt', version='3.18.3')

import java.io.File
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.security.spec.PKCS8EncodedKeySpec
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.JWT
import groovy.json.JsonSlurper
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
    pi(longOpt: 'private', 'Set private key', args: 1, required: true);
    pu(longOpt: 'public', 'Set public key', args: 1, required: true);
}

def options = cli.parse(args)
def publicFile  = options.pu
def privateFile = options.pi
def json        = options.arguments()[0]

if (!json || !publicFile || !privateFile) {
    System.err.println('usage: example_jwt_encode.groovy --public <file> --private <file> payload')
    System.exit(1)
}

try {
    def pubKey    = getPublicKey(publicFile)
    def privKey   = getPrivateKey(privateFile)

    def payload   = new JsonSlurper().parseText(new File(json).getText('UTF-8'))

    def kid       = pubKey.getEncoded().digest('SHA-256')
    def algorithm = Algorithm.ECDSA256(pubKey,privKey)
    def token     = JWT.create()
                       .withHeader(["kid":kid])
                       .withPayload(payload)
                       .sign(algorithm)

    println(token)
}
catch (ex) {
    System.err.println(ex)
    System.exit(2)
}
