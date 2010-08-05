package com.clarkparsia.pelletserver.scala.results

import com.clarkparsia.pelletserver.scala.api.response.{Result,PlainJsonResponse,SparqlBooleanResponse}
import dispatch.Http
import dispatch.json.JsHttp.{Request2JsonRequest, sym_add_operators}
import dispatch.json.JsValue
import dispatch.json.Js._

object PlainJsonResult {
	class PlainJsonResultWrapper(f: String => (Result with PlainJsonResponse)) {
		def asJson: JsValue = {
			Http(f("text/json").req ># identity)
		}
	}
	implicit def wrapJson(f: String => (Result with PlainJsonResponse)): PlainJsonResultWrapper = {
		new PlainJsonResultWrapper(f)
    }
}

object SparqlBooleanResult {
	class ResultWrapper(f: String => (Result with SparqlBooleanResponse)) {
		def asBoolean: Boolean = {
			Http(f("application/sparql-results+json").req ># { 'boolean ! bool })
		}
	}
	implicit def wrapSparqlBool(f: String => (Result with SparqlBooleanResponse)): ResultWrapper = {
		new ResultWrapper(f)
    }
}