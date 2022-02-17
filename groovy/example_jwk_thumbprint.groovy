#!/usr/bin/env groovy

@Grab(group='com.nimbusds', module='nimbus-jose-jwt', version='9.19')

import com.nimbusds.jose.*
import com.nimbusds.jose.jwk.*

def jwkPublicKey(json) {
    def jwk = JWK.parse(json)
    return jwk
}

def cli = new CliBuilder()

def options = cli.parse(args)

def jwkFile = options.arguments()[0]

if ( ! jwkFile ) {
    System.err.println("usage: example_jwk_pem.groovy jwk_file")
    System.exit(1)
} 

def jwkJson = new File(jwkFile).text;

def publicKey = jwkPublicKey(jwkJson)

println "Public key:" + publicKey
println "Thumbprint:" + publicKey.computeThumbprint()