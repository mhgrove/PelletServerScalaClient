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

package com.clarkparsia.pelletserver.scala.api

import dispatch.Request
import dispatch.Http.str2req
import scala.util.matching.Regex
import java.net.URI
import com.clarkparsia.pelletserver.scala.api.json.{JKnowledgeBase, JService}
import com.clarkparsia.pelletserver.scala.api.schema.AbstractKnowledgeBase

package response {
	sealed abstract trait ResultsFormat
	trait PlainJsonResponse extends ResultsFormat
	trait RdfResponse extends ResultsFormat
	trait RdfXmlResponse extends ResultsFormat
	trait SparqlResponse extends ResultsFormat
	trait PlainXmlResponse extends ResultsFormat
	trait SparqlXmlResponse extends ResultsFormat
	trait SparqlJsonResponse extends ResultsFormat
	trait SparqlBooleanResponse extends ResultsFormat
	
	class Result(r: Request) {
		val req = r
	}
}
import response._

class KnowledgeBase(info: JKnowledgeBase) extends AbstractKnowledgeBase {

  def hasName() = List(info.name)
  def hasService() = Nil // TODO: not currently used; reasons to keep?
  override def toString() = {
	  "KnowledgeBase(%s)".format(info.name)
  }
  
  private def fillTemplate(template: String, params: Map[String, String]) = {
	  var queryParams: List[Pair[String, String]] = Nil
	  val uriNoQuery = new Regex("""\{(\?)?(.*?)\}""").replaceAllIn(template, m => {
	 	  val queryOption = m.group(1) != null
	 	  val expansions = for (v <- m.group(2).split(',');
	 	       					if (params.contains(v) && (params(v) != null))) yield (v, params(v))
	 	  if (queryOption) {
	 	 	  queryParams = queryParams ::: expansions.toList
	 	 	  ""
	 	  } else {
	 	 	  expansions.map(ve => ve._2).mkString(",")
	 	  }
	  })
	  (uriNoQuery, queryParams)
  }
  
  private def fetchResponse(s: JService, params: Map[String, String] = Map()): String => Request = {
      mimetype => {
    	  val (uriNoQuery, query) = fillTemplate(s.endpoint.url, params)
    	  val req = uriNoQuery <:< Map("Accept" -> mimetype)
    	  val size = query.foldRight(uriNoQuery.length + 1)((x,y) => x._1.length + x._2.length + y + 1)
    	  if ((size > 255) && (s.endpoint.`http-methods`.contains("POST"))) {
    		  req << query.toMap
    	  } else {
    	 	  req <<? query.toMap
    	  }
	  }
  }
