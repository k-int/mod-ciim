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
import groovy.json.JsonOutput

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

// this.configure('cardinal_si');
println("setup");
this.configure('palci_si');
println("login");
this.login();
println("refdata");
this.loadRefdata('IdentifierTypes');
this.loadRefdata('Classns');
this.loadRefdata('ContributorNameTypes');
this.loadRefdata('InstanceTypes');
println("enumerate");
this.enumerateInventory();
println("exit");
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
      // request.uri.path= '/bl-users/login'
      request.uri.path= '/authn/login'
      // request.uri.query=[expandPermissions:true,fullPermissions:true]
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
  r = [ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' ]

  File tsv_file = new File('./log.tsv');
  if ( tsv_file.exists() ) {
    tsv_file.renameTo('./log.tsv.bak');
    tsv_file = new File('./log.tsv');
  }

  tsv_file << 'cursor	elapsed'
  r.each { o1 ->
    r.each { o2 ->
      r.each { o3 ->
        enumerateInventoryByQuery("${o1}${o2}${o3}", tsv_file);
      }
    }
  }
}

def enumerateInventoryByQuery(String q, File tsv_file) {

  String cursor = '0001-01-01T00:00:00.000'
  Long offset = -1;
  long records_processed = 1
  Map r = null;
  int c = 0;
  // Artificially restrict ourselves to 5 pages of data


  String MODE='offset'

  while ( records_processed > 0 ) {
    println("Fetch page of data starting at ${cursor}/${offset}");
    r = getPageOfInventoryDataSince(q, cursor, tsv_file, offset+1, MODE )

    println("Process records");
    // If mode is timestamp set this
    r?.records?.each { record ->
      saveJson(record)
    }

    records_processed = r?.records?.size() ?: 0
    tsv_file << "${q}	${offset}	${r.cursor}	${r.maxCursor}	${r.totalRecords}	${records_processed}	${r.elapsed}\n"

    if ( MODE=='cursor' )
      cursor = r.maxCursor;

    offset = r.offset

    // Don't ddos 
    Thread.sleep(3000);

    println("maxCursor:${r.maxCursor}");
  }

  println("got ${records_processed} records");
}

def saveJson(record) {
  String record_id = record.id
  String record_bucket = "data/${record_id[0]}/${record_id[1]}/${record_id[2]}"
  File record_bucket_dir = new File(record_bucket)

  if ( ! record_bucket_dir.exists() )
    record_bucket_dir.mkdirs();

  File record_file = new File("${record_bucket}/${record_id}")

  if ( record_file.exists() )
    record_file.delete();

  record_file << JsonOutput.toJson(record)
}

// Mode controls the pagination method - offset or the updatedDate field. updatedDate behaves... oddly.
def getPageOfInventoryDataSince(String query, String cursor, File tsv_file, Long offset, String mode='offset') {

  println("getPageOfInventoryDataSince(${cursor},...${offset},${mode}");
  long start_time = System.currentTimeMillis();

  def fc = this.getClient()
  Map result = [
    records:[]
  ]
  result.records_processed = 0;

  println("Get page of data updatedDate > ${cursor}/${offset}");

  // Limit and Offset
  fc.get {
    request.uri.path='/instance-storage/instances'
    request.headers.'X-Okapi-Tenant'=this.tenant;
    request.headers.'X-Okapi-Token'=this.session_ctx.auth
    request.headers.'accept'='application/json'
    request.uri.query= [
      'limit':250,
    ]

    switch ( mode) {
      case 'offset':
        request.uri.query.offset = offset
        // request.uri.query.query='(metadata.updatedDate>'+cursor+') sortBy metadata.updatedDate/sort.ascending';
        // request.uri.query.query='(metadata.updatedDate>'+cursor+')'
        request.uri.query.query='query=(id="'+query+'*") sortby id'
        break;
      case 'timestamp':
        // request.uri.query.query='(metadata.updatedDate>'+cursor+'a)  sortby metadata.updatedDate/sort.ascending';
        // request.uri.query.query='(metadata.updatedDate>'+cursor+'a)'
        request.uri.query.query='query=(id="'+query+'*") sortby id'
        break;
    }

    response.failure { FromServer fs, Object body ->
      println("Problem ${body} ${fs} ${fs.getStatusCode()}");
    }

    response.success { FromServer fs, Object body ->
      // println("OK ${body} ${fs} ${fs.getStatusCode()}");
      println("Total records: ${body.totalRecords}");

      result.totalRecords = body.totalRecords
      body.instances.each { inst ->
        result.records.add(inst);
        // dumpSummary(result.records_processed++, inst)
        // println(toTSV(inst));
        // tsv_file << toTSV(inst)+'\n';
        result.maxCursor=inst.metadata.updatedDate;
        result.offset = (offset++)
      }
    }
  }

  result.elapsed = System.currentTimeMillis()-start_time
  println("Elapsed: ${result.elapsed}");
  return result;
}

void dumpSummary(c, inst) {
  println("${c} ${inst.id} ${inst.metadata.updatedDate} ${inst.title}");
}

void dumpFullInstance(inst) {
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
    else if ( k == 'publication' ) {
      println("  Subjects");
      inst.publication?.each { pub ->
        println("    ${pub.publisher} ${pub.dateOfPublication}");
      }
    }
    else {
      println("  ${k} -> ${v}")
    }
  }
  println("UpdatedDate: ${inst.metadata.updatedDate}");
}

String toTSV(inst) {
  return toTSVData(inst).join('\t')
}

String[] toTSVData(inst) {

  String[] result = [
    inst.id,
    inst.title,
    inst.publication ? inst.publication[0].publisher : '',
    inst.publication ? inst.publication[0].dateOfPublication : '',
    inst.publication ? inst.publication[0].place : '',
    inst.series ? inst.series[0] : ''
  ]

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
