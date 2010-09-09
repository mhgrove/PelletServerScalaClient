package com.clarkparsia.pelletserver.scala.api.json

// JSON extraction classes
// Based on server-version 0.9.2
case class JServerInformation(`description-created`: Option[java.util.Date], `administrative-contact`: String,  `server-version`: String)  
case class JEndpoint(`http-methods`: List[String], url: String)
case class JService(`response-mimetype`: List[String], `supported-entailment-profile`:Option[String], endpoint: JEndpoint)
case class JKnowledgeBase(name: String, `kb-services`: Map[String, JService]) 
case class JServiceDescription(`ps-discovery`: JService, `knowledge-bases`: List[JKnowledgeBase], `server-information`: JServerInformation)
