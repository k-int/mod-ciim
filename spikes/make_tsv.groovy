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
import groovy.json.JsonSlurper

def jsonSlurper = new JsonSlurper()


import static groovy.io.FileType.FILES

def dir = new File("./data");
println("dir: ${dir}");
int ctr=0;

println("record_id\ttitle\tpublicationdate\toclcnum\tissn\tisbn\tprimaryauthor\tpublisher\tpubyear");

dir.traverse(type: FILES, maxDepth: 4) { 
  def citation = jsonSlurper.parse(it)
  // println("[${ctr++}] ${citation.id}")
  String oclc = ( citation.identifiers.find { it.identifierTypeId=='7e591197-f335-4afb-bc6d-a6d76ca3bace' } )?.value
  // String issn_number = ( citation.identifiers.find { it.identifierTypeId=='7e591197-f335-4afb-bc6d-a6d76ca3bace' } )?.value
  String issn = null
  String isbn = ( citation.identifiers.find { it.identifierTypeId=='8261054f-be78-422d-bd51-4ed9f33c3422' } )?.value
  String primaryauthor = ( citation.contributors.find { it.primary = true } )?.name
  println("${citation.id}\t"+
          "${citation.title}\t"+
          "${citation?.publication?.publicationDate?:''}\t"+
          "${oclc?:''}\t"+
          "${issn?:''}\t"+
          "${isbn?:''}\t"+
          "${primaryauthor?:''}"+
          "${citation?.publication?.publisher?:''}\t"+
          "${citation?.publication?.dateOfPublication?:''}")
};

println("done");
