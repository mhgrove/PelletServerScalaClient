package com.clarkparsia.pelletserver.scala.vocab

import com.hp.hpl.jena.rdf.model.ModelFactory

/**
 * PelletServer RDF vocabulary terms, declared the same way as Jena does it.
 */
object PelletServer {

  private val model = ModelFactory.createDefaultModel();

  val NS = "tag:clarkparsia.com,2010-06-21:pelletserver:"  
  def getURI() = NS
  val NAMESPACE = model.createResource(NS)
  
  val `pellet-server-schema` = model.createResource("http://ps.clarkparsia.com/schema/pellet-server-schema")

  val PelletServer = model.createResource(NS + "PelletServer")
  val WebResource = model.createResource(NS + "WebResource")
  val Service = model.createResource(NS + "Service")
  val KnowledgeBase = model.createResource(NS + "KnowledgeBase")
  
  val hasService = model.createProperty(NS + "hasService")
  val hasHttpMethod = model.createProperty(NS + "hasHttpMethod")
  val hasResponseMimeType = model.createProperty(NS + "hasResponseMimeType")
  val hasUriTemplate = model.createProperty(NS + "hasUriTemplate")
  
}
