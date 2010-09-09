package com.clarkparsia.pelletserver.scala

import java.net.URL
import java.io.InputStreamReader
import dispatch.json.JsValue
import dispatch.:/
import dispatch.Http
import dispatch.Http.str2req
import dispatch.json.JsHttp.Request2JsonRequest
import com.clarkparsia.pelletserver.scala.api.{KnowledgeBase, SDProvenance}
import com.clarkparsia.pelletserver.scala.api.json.JServiceDescription
import scala.math.Ordering.{Iterable, String}
import org.apache.commons.logging.LogFactory

/**
 * Instances of PelletServer are created from a service description document in JSON,
 * referenced as a URL.
 * 
 * This is the main entry point to using a remote Pellet Server instance.
 * 
 */
class PelletServer(serviceDescription: URL) {
	
	lazy private val log = LogFactory.getLog(this.getClass.getName)

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
	
	// extract the JSON object into our own case classes.
	private val sd = CustomExtractor.extractServiceDescription(json)
	
	// check version against that used to generate the source
	private val versionComparison = Iterable(String).compare(
					sd.`server-information`.`server-version`.split('.'),
					SDProvenance.SD_VERSION.split('.'))
	if (versionComparison != 0) {
		log.warn("PelletServer service description version %s than expected.  Got version %s, expected %s.".format(
					if (versionComparison < 0) "older" else "newer",
					sd.`server-information`.`server-version`,
					SDProvenance.SD_VERSION))
	}
	
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