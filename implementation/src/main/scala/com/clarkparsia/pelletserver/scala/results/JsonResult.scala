package com.clarkparsia.pelletserver.scala.results

import com.clarkparsia.pelletserver.scala.api.{Result,PlainJsonResponse,SparqlBooleanResponse}
import dispatch.Http
import dispatch.liftjson.Js._
import net.liftweb.json.JsonAST.{JValue,JField,JString}

object PlainJsonResult {
	class PlainJsonResultWrapper(f: String => (Result with PlainJsonResponse)) {
		def asJson: JValue = {
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
			Http(f("application/sparql-results+json").req ># { json =>
				(json \ "results" \ "bindings")(0) \\ "value" match {
					case JField("value", JString("true")) => true
					case JField("value", JString("false")) => false
					case _ => throw new Exception() //TODO: Specialize exception
				}
			})
		}
	}
	implicit def wrapSparqlBool(f: String => (Result with SparqlBooleanResponse)): ResultWrapper = {
		new ResultWrapper(f)
    }
}