package com.clarkparsia.pelletserver.scala.results

import com.clarkparsia.pelletserver.scala.api.{Result,PlainJsonResponse,SparqlBooleanResponse}
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
			val bindings = Http(f("application/sparql-results+json").req ># {
				'results ! obj andThen 'bindings ! list
			})
			bindings match {
				case head :: tail =>
				    ('Consistent ! obj andThen 'value ! str)(head) match {
				    	case "true" => true
				    	case "false" => false
				    	case e @ _ => error("Expecting true or false for boolean result, got " + e.toString)
				    }
				case Nil => error("Expecting a value for 'Consistency', got nothing")
			}
		}
	}
	implicit def wrapSparqlBool(f: String => (Result with SparqlBooleanResponse)): ResultWrapper = {
		new ResultWrapper(f)
    }
}