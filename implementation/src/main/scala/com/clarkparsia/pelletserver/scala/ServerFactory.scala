package com.clarkparsia.pelletserver.scala

import java.net.URL
import java.io.InputStreamReader
import net.liftweb.json.JsonParser.parse
import dispatch.:/
import dispatch.Http
import dispatch.Http.str2req
import dispatch.liftjson.Js.Request2JsonRequest
import net.liftweb.json.{Formats, DateFormat}
import java.util.{Date, GregorianCalendar, TimeZone}
import javax.xml.datatype.DatatypeFactory
import com.clarkparsia.pelletserver.scala.api.{KnowledgeBase,JServiceDescription}

/** 
 * Service description uses XML date/time format, so this object can be used to
 * parse and format XML dates for the JSON extractors.
 * 
 */

object SDFormats extends Formats {
	val dtf = DatatypeFactory.newInstance
	val dateFormat = new DateFormat {
		def parse(s: String): Option[Date] = {
			try {
				val gc = dtf.newXMLGregorianCalendar(s).toGregorianCalendar
				Some(gc.getTime)
			} catch {
				case e: Exception => None
			}
		}
		def format(d: Date) = {
			val gc = new GregorianCalendar(TimeZone.getTimeZone("UTC"))
			gc.setTime(d)
			dtf.newXMLGregorianCalendar(gc).toXMLFormat
		}
	}
}

/**
 * Instances of PelletServer are created from a service description document in JSON,
 * referenced as a URL.
 * 
 * This is the main entry point to using a remote Pellet Server instance.
 * 
 */
class PelletServer(serviceDescription: URL) {

	private val json = if ("file".equalsIgnoreCase(serviceDescription.getProtocol)) {
		parse(new InputStreamReader(serviceDescription.openStream))
	} else {
		// The databinder.dispatch syntax can be opaque.
		// The <:< method takes a request and adds the given headers; the serviceDescription string
		// is implicitly converted to a request object by the import of the dispatch.Http.str2req function.
		// The output from this request is parsed as JSON using the ># function, which itself takes
		// a function, in this case the identity function which just returns the parsed JSON object.
		Http(serviceDescription.toString <:< Map("Accept" -> "text/json") ># { identity })
	}
	
	// Use the XML date format parser and default parsers...
	private implicit val formats = SDFormats
	// to extract the JSON object into our own case classes.
	private val sd = json.extract[JServiceDescription]
	
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