#!/usr/bin/env groovy

@Grab(group='com.auth0', module='java-jwt', version='3.18.3')

import com.auth0.jwt.*
import java.util.Base64
import java.nio.charset.StandardCharsets
import groovy.cli.commons.CliBuilder

def cli = new CliBuilder()

def options = cli.parse(args)

if (options.arguments().size() != 1) {
   System.err.println("usage: example_jwt_decode.groovy jwt")
   System.exit(1)
}

def jwtStr   = options.arguments()[0]

def jwt = JWT.decode(jwtStr)

def headerJson = new String(
                    Base64.getUrlDecoder().decode(
                        jwt.getHeader()
                    ), StandardCharsets.UTF_8)

def payloadJson = new String(
                    Base64.getUrlDecoder().decode(
                        jwt.getPayload()
                    ), StandardCharsets.UTF_8) 

def output = "{\"header\":" + headerJson + ",\"payload\":" + payloadJson + "}"

System.out.println(output)