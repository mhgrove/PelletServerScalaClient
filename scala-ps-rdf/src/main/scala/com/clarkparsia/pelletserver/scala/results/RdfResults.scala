package com.clarkparsia.pelletserver.scala.results

import com.clarkparsia.pelletserver.scala.api.{Result,RdfResponse}
import com.hp.hpl.jena.rdf.model.{Model, ModelFactory}
import dispatch.Http

object RdfResult {
	class RdfResultWrapper(f: String => (Result with RdfResponse)) {
		def asRdf: Model = {
			val request = f("application/rdf+xml").req
			Http(request >> (stream => {
				val model = ModelFactory.createDefaultModel 
				model.read(stream, request.to_uri.toString)
			}))
		}
	}
	implicit def wrapRdf(f: String => (Result with RdfResponse)) = {
		new RdfResultWrapper(f)
	}
}
