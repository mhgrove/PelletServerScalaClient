package com.clarkparsia.pelletserver.scala

import dispatch.json._
import dispatch.json.Js._
import com.clarkparsia.pelletserver.scala.api._
import javax.xml.datatype.DatatypeFactory
import java.util.{Date, GregorianCalendar, TimeZone}

/** 
 * Service description uses XML date/time format, so this object can be used to
 * parse and format XML dates for the JSON extractors.
 * 
 */

object SDFormats {
	val dtf = DatatypeFactory.newInstance
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

object CustomExtractor {

	private val psDiscovery = Symbol("ps-discovery") ! obj
	private val kbs = Symbol("knowledge-bases") ! list
	private val kbServices = Symbol("kb-services") ! obj
	private val serverInfo = Symbol("server-information") ! obj
	private val descriptionCreated = Symbol("description-created") ! str
	private val adminContact = Symbol("administrative-contact") ! str
	private val serverVersion = Symbol("server-version") ! str
	private val responseMimetype = Symbol("response-mimetype") ! list
	private val httpMethods = Symbol("http-methods") ! list
	
	def extractServiceDescription(v: JsValue) = {
		JServiceDescription(
		    extractService(psDiscovery(v)),
		    kbs(v).map(extractKnowledgeBase(_)),
		    extractInfo(serverInfo(v))
		)
	}
	
	private def extractInfo(v: JsObject) = {
		val date = SDFormats.parse(descriptionCreated(v))
		JServerInformation(date, adminContact(v), serverVersion(v))
	}
	
	private def extractService(v: JsObject) = {
		JService(
		    responseMimetype(v).map(_.toString),
		    v.self.get(JsString("supported-entailment-profile")).map(_.toString),
		    extractEndpoint(('endpoint ! obj)(v))
		)
	}
	
	private def extractEndpoint(v: JsObject) = {
		JEndpoint(
			httpMethods(v).map(_.toString),
			('url ! str)(v)
		)
	}
	
	private def extractKnowledgeBase(v: JsValue) = {
		JKnowledgeBase(
			('name ! str)(v),
			for ((k,v) <- kbServices(v).self) yield {
				(k.self -> (v match {
					case JsObject(o) => extractService(JsObject(o))
					case e @ _ => error("Expecting JSON object defining server, got " + e.toString)
				}))
			}
		)
	}
}