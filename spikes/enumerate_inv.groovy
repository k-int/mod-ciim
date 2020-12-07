#!/usr/bin/env groovy

@Grapes([
  @GrabResolver(name='mvnRepository', root='http://central.maven.org/maven2/'),
  @GrabResolver(name='kint', root='http://nexus.k-int.com/content/repositories/releases'),
  @Grab(group='io.github.http-builder-ng', module='http-builder-ng-core', version='1.0.4'),
  @Grab(group='commons-codec', module='commons-codec', version='1.14'),
  @Grab(group='org.ini4j', module='ini4j', version='0.5.4'),
  @Grab(group='io.jsonwebtoken', module='jjwt-impl', version='0.11.0'),
  @Grab(group='io.jsonwebtoken', module='jjwt-jackson', version='0.11.0')
])

import org.ini4j.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import groovyx.net.http.HttpBuilder
import groovyx.net.http.FromServer
import static groovyx.net.http.HttpBuilder.configure
import io.jsonwebtoken.Claims;
import java.security.PublicKey
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec

/**
 * This script depends upon a file ~/.folio/credentials set out as follows:
 * [configname]
 * url=https://some.url:9130
 * tenant=tenantstr
 * password=pass
 * username=user
 * 
 * This is then called with configure('configname')
 */

url=null;
username=null;
password=null;
tenant=null;
jwt=null;
folio_api = null;
session_ctx=[:]

this.configure('cardinal_si');
this.login();
this.enumerateInventory();

System.exit(0);

public void configure(String cfgname) {
  this.cfgname = cfgname;
  Wini ini = new Wini(new File(System.getProperty("user.home")+'/.folio/credentials'));
  this.url = ini.get(cfgname, 'url', String.class);
  this.username = ini.get(cfgname, 'username', String.class);
  this.password = ini.get(cfgname, 'password', String.class);
  this.tenant = ini.get(cfgname, 'tenant', String.class);
}

public HttpBuilder getClient() {
  if ( this.folio_api == null ) {
    println("Intialize httpbuilder :: ${this.url}");
    this.folio_api = configure {
      request.uri = this.url
    }
  }

  return this.folio_api;
}


def login() {

  def postBody = [username: this.username, password: this.password]
  def fc = this.getClient()

  if ( fc != null ) {
    println("attempt login ${postBody}");
    fc.post {
      request.uri.path= '/bl-users/login'
      request.uri.query=[expandPermissions:true,fullPermissions:true]
      request.headers.'X-Okapi-Tenant'=this.tenant;
      request.headers.'accept'='application/json'
      request.headers.'Content-Type'='application/json'
      request.contentType='application/json'
      request.body=postBody
      response.failure { FromServer fs, Object body ->
        println("Problem ${body} ${fs} ${fs.getStatusCode()}");
      }
      response.success { FromServer fs, Object body ->
        println("OK ${body} ${fs} ${fs.getStatusCode()}");
        println("Logged In")
        def tok_header = fs.headers?.find { h-> h.key == 'x-okapi-token' }
        if ( tok_header ) {
          // result = tok_header.value;
          session_ctx.auth = tok_header.value
        }
        else {
          println("Unable to locate okapi token header amongst ${r1?.headers}");
        }

      }
    }
  }
  else {
    println("No client available");
  }
}

def enumerateInventory() {

  def fc = this.getClient()

  fc.get {
    request.uri.path='/instance-storage/instances'
    request.headers.'X-Okapi-Tenant'=this.tenant;
    request.headers.'X-Okapi-Token'=this.session_ctx.auth
    request.headers.'accept'='application/json'
    request.uri.query= [
      'metadata.updatedDate':'0001-01-01T00:00:00.000'
    ]
    response.failure { FromServer fs, Object body ->
      println("Problem ${body} ${fs} ${fs.getStatusCode()}");
    }
    response.success { FromServer fs, Object body ->
      println("OK ${body} ${fs} ${fs.getStatusCode()}");
    }
  }
}
