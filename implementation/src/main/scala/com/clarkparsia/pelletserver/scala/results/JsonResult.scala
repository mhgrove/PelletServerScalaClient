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