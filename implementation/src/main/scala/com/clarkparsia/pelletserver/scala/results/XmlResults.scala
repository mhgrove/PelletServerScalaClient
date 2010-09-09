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

package com.clarkparsia.pelletserver.scala.results

import com.clarkparsia.pelletserver.scala.api.response.{Result,RdfXmlResponse,SparqlXmlResponse,SparqlResponse}
import dispatch.Http
import java.net.URI

object RdfXmlResult {
	class RdfXmlResultWrapper(f: String => (Result with RdfXmlResponse)) {
		def asRdfXml: scala.xml.Elem = {
			Http(f("application/rdf+xml").req <> identity)
		}
	}
	implicit def wrapRdfXml(f: String => (Result with RdfXmlResponse)) = {
		new RdfXmlResultWrapper(f)
    }
}

object SparqlXmlResult {
	class SparqlXmlResultWrapper(f: String => (Result with SparqlXmlResponse)) {
		def asSparqlXml: scala.xml.Elem = {
			Http(f("application/sparql-results+xml").req <> identity)
		}
	}
	implicit def wrapSparqlXml(f: String => (Result with SparqlXmlResponse)) = {
		new SparqlXmlResultWrapper(f)
	}
}

sealed abstract class SparqlResult
case class Literal(literal: String) extends SparqlResult
trait Language { def lang: String }
trait Datatype { def datatype: URI }
case class Resource(uri: URI) extends SparqlResult
case class BlankNode(label: String) extends SparqlResult

object SparqlResult {
	class SparqlResultWrapper(f: String => (Result with SparqlResponse)) {
		def asSparql: Seq[Map[String, SparqlResult]] = {
			Http(f("application/sparql-results+xml").req <> { xml =>
				for (result <- xml \ "results" \ "result") yield {
					val kvs = (for (binding <- result \ "binding") yield {
						val key = binding \ "@name" text
						val value = binding \ "_" first match {
							case <uri>{uri}</uri> =>
								Resource(new URI(uri text))
							case l @ <literal>{literal}</literal> =>
								l.attribute("http://www.w3.org/XML/1998/namespace", "lang") match {
									case Some(language) => new Literal(literal text) with Language { val lang = language text }
									case None => l.attribute("datatype") match {
										case Some(dt) => new Literal(literal text) with Datatype { val datatype = new URI(dt text) }
										case None => Literal(literal text)
									}
								}
							case <bnode>{bnode}</bnode> =>
								BlankNode(bnode text)
						}
						(key -> value)
					})
					kvs.toMap
				}
			})
		}
	}
	implicit def wrapSparql(f: String => (Result with SparqlResponse)) = {
		new SparqlResultWrapper(f)
	}
}