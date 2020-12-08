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
session_ctx=[
  auth:'',
  refdata:[:]
]

this.configure('cardinal_si');
this.login();
this.loadRefdata('IdentifierTypes');
this.loadRefdata('Classns');
this.loadRefdata('ContributorNameTypes');
this.loadRefdata('InstanceTypes');
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
        // println("OK ${body} ${fs} ${fs.getStatusCode()}");
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
  String cursor = '0001-01-01T00:00:00.000'
  Map r = null;
  int c = 0;
  // Artificially restrict ourselves to 5 pages of data
  while ( c++ < 5 ) {
    println("Fetch page of data starting at ${cursor}");
    r = getPageOfInventoryDataSince(cursor)
    cursor = r.maxCursor;

    // Don't ddos 
    Thread.sleep(5000);
  }

}

def getPageOfInventoryDataSince(String cursor) {

  def fc = this.getClient()
  Map result = [:]

  fc.get {
    request.uri.path='/instance-storage/instances'
    request.headers.'X-Okapi-Tenant'=this.tenant;
    request.headers.'X-Okapi-Token'=this.session_ctx.auth
    request.headers.'accept'='application/json'
    request.uri.query= [
      'query':'metadata.updatedDate>'+cursor
    ]
    response.failure { FromServer fs, Object body ->
      println("Problem ${body} ${fs} ${fs.getStatusCode()}");
    }
    response.success { FromServer fs, Object body ->
      // println("OK ${body} ${fs} ${fs.getStatusCode()}");
      println("Total records: ${body.totalRecords}");
      body.instances.each { inst ->
        println("\n\nInstance:");
        inst.each { k,v -> 
          if ( v instanceof String ) {
            println("  ${k} -> ${v}");
          }
          else if ( k == 'identifiers' ) {
            println("  Identifiers");
            inst.identifiers?.each { id ->
              // Resolve identifierTypeId using previously looked up refdata
              println("    [${this.session_ctx.refdata.IdentifierTypes[id.identifierTypeId]?.name}] ${id.value}");
            }
          }
          else if ( k == 'contributors' ) {
            println("  Contributors");
            inst.contributors?.each { id ->
              // Resolve identifierTypeId using previously looked up refdata
              println("    [${this.session_ctx.refdata.ContributorNameTypes[id.contributorNameTypeId]?.name}] ${id.name}");
            }
          }
          else if ( k == 'subjects' ) {
            println("  Subjects");
            inst.subjects?.each { subject ->
              println("    ${subject}");
            }
          }
          else {
            println("  ${k} -> ${v}")
          }
        }
        println("UpdatedDate: ${inst.metadata.updatedDate}");
        result.maxCursor=inst.metadata.updatedDate;
      }
    }
  }

  return result;
}


//
// https://folio-snapshot-okapi.dev.folio.org/identifier-types?limit=1000&query=cql.allRecords=1%20sortby%20name
Map loadRefdata(String type) {

  Map result = session_ctx.refdata.get(type);

  if ( result == null ) {
    result = [:]
    session_ctx.refdata[type]=result;

    String service_path = null;
    switch ( type ) {
      case 'IdentifierTypes':
        service_path = '/identifier-types'
        response_key='identifierTypes'
        break;
      case 'Classns':
        service_path = '/classification-types'
        response_key='classificationTypes'
        break;
      case 'ContributorNameTypes':
        service_path = '/contributor-name-types'
        response_key='contributorNameTypes'
        break;
      case 'InstanceTypes':
        service_path = '/instance-types'
        response_key='instanceTypes'
        break;
    }

    if ( service_path != null ) {
      this.getClient().get {
        request.uri.path = service_path
        request.headers.'X-Okapi-Tenant'=this.tenant;
        request.headers.'X-Okapi-Token'=this.session_ctx.auth
        request.headers.'accept'='application/json'
        request.uri.query= [
          'limit':500,
          'query':'cql.allRecords=1%20sortby%20name'
        ]
        response.failure { FromServer fs, Object body ->
          println("Problem ${body} ${fs} ${fs.getStatusCode()}");
        }
        response.success { FromServer fs, Object body ->
          body[response_key].each { refdata ->
            result[refdata.id] = refdata;
          }
        }
      }
    }
  }

  return result;
}
