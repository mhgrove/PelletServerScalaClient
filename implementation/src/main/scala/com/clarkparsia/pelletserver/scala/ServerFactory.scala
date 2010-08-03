package com.clarkparsia.pelletserver.scala

import java.net.URL
import java.io.InputStreamReader
//import net.liftweb.json.JsonParser.parse
import dispatch.json.JsValue
import dispatch.:/
import dispatch.Http
import dispatch.Http.str2req
import dispatch.json.JsHttp.Request2JsonRequest
//import net.liftweb.json.{Formats, DateFormat}
import com.clarkparsia.pelletserver.scala.api.{KnowledgeBase,JServiceDescription}

/**
 * Instances of PelletServer are created from a service description document in JSON,
 * referenced as a URL.
 * 
 * This is the main entry point to using a remote Pellet Server instance.
 * 
 */
class PelletServer(serviceDescription: URL) {

	private val json = if ("file".equalsIgnoreCase(serviceDescription.getProtocol)) {
		JsValue.fromStream(serviceDescription.openStream)
	} else {
		// The databinder.dispatch syntax can be opaque.
		// The <:< method takes a request and adds the given headers; the serviceDescription string
		// is implicitly converted to a request object by the import of the dispatch.Http.str2req function.
		// The output from this request is parsed as JSON using the ># function, which itself takes
		// a function, in this case the identity function which just returns the parsed JSON object.
		Http(serviceDescription.toString <:< Map("Accept" -> "text/json") ># { identity })
	}
	
	// to extract the JSON object into our own case classes.
	//private val sd = json.extract[JServiceDescription]
	private val sd = CustomExtractor.extractServiceDescription(json)
	
	/**
	 * Returns the list of knowledge bases available from the remote Pellet Server.
	 * 
	 */
	def getKBs() = {
		for (kb <- sd.`knowledge-bases`) yield {
			new KnowledgeBase(kb)
		}
	}
	
	/**
	 * Returns the version number of the remote Pellet Server
	 */
	def getVersion() = {
		sd.`server-information`.`server-version` 
	}
	
	/**
	 * Returns the administrative contact email address for the remote Pellet Server.
	 * 
	 */
	def getAdminContact() = {
		sd.`server-information`.`administrative-contact`
	}
}

/**
 * Companion object acting as a factory object to create new PelletServer instances
 * from a service description URL, either as a java.net.URL, or a String.
 * 
 */

object PelletServer {
	def apply(serviceDescription: URL) = {
		new PelletServer(serviceDescription)
	}
	def apply(serviceDescription: String) = {
		new PelletServer(new URL(serviceDescription))
	}
}