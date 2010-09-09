/**
 * Copyright (C) 2010 Clark and Parsia LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
