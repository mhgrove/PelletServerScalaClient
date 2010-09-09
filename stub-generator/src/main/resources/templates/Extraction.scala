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

package com.clarkparsia.pelletserver.scala.api.json

// JSON extraction classes
// Based on server-version 0.9.2
case class JServerInformation(`description-created`: Option[java.util.Date], `administrative-contact`: String,  `server-version`: String)  
case class JEndpoint(`http-methods`: List[String], url: String)
case class JService(`response-mimetype`: List[String], `supported-entailment-profile`:Option[String], endpoint: JEndpoint)
case class JKnowledgeBase(name: String, `kb-services`: Map[String, JService]) 
case class JServiceDescription(`ps-discovery`: JService, `knowledge-bases`: List[JKnowledgeBase], `server-information`: JServerInformation)
