package com.clarkparsia.pelletserver.scala

import com.hp.hpl.jena.rdf.model.ModelFactory
import java.net.URL
import java.io.{File, FileWriter, Writer}
import _root_.org.mindswap.pellet.jena.PelletReasonerFactory
import com.hp.hpl.jena.vocabulary.{OWL, XSD, DCTerms}
import com.hp.hpl.jena.ontology.OntClass
import vocab.PelletServer
import scala.collection.JavaConversions.asIterator
//import com.hp.hpl.jena.ontology.Individual
import scala.util.matching.Regex
import net.liftweb.json.JsonParser.parse
import net.liftweb.json.JsonAST.{JValue,JField,JString,JObject}
import java.io.InputStreamReader
import scala.io.Source
import javax.xml.datatype.DatatypeFactory
import java.util.{Date, GregorianCalendar, TimeZone}

object StubGenerator {

	val ROOT_PACKAGE = "com.clarkparsia.pelletserver.scala.api"
	val ABSTRACT_PACKAGE = ROOT_PACKAGE + ".schema"
	val EXTRACTION_PACKAGE = ROOT_PACKAGE + ".json"
	val METHOD_PACKAGE = ROOT_PACKAGE + ".kb"
	val TOP_CLASS = PelletServer.WebResource
  
	val schema = ModelFactory.createDefaultModel
	var rootOutputDirectory = new File("target/generated-sources/ps")
  
	val RESERVED = List("abstract", "case", "catch", "class", "def", 
						"do", "else", "extends", "false", "final", "finally",
						"for", "if", "implicit", "import", "match", "mixin",
						"new", "null", "object", "override", "package", 
						"private", "protected", "requires", "return", "sealed", 
						"super", "this", "trait", "true", "try", 
						"type", "val", "var", "while", "with", "yield")
				      
	val ABSTRACT_SCALA = "Abstract.scala"
	val KB_SCALA = "KnowledgeBase.scala"
	val EXTRACTION_SCALA = "Extraction.scala"
	  
	def loadSchema(url: URL) {
		schema.read(url.toString)
	}
  
	def getSchemaVersion() = {
		for (versionStatement <- Option(schema.getProperty(PelletServer.`pellet-server-schema`, DCTerms.hasVersion))) yield {
			versionStatement.getString
		}
	}
  
	def setOutputDirectory(dir: File) {
		rootOutputDirectory = dir
	}
  
	def generateAbstractHierarchy() {
		val ontModel = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC, schema)
		val top = ontModel.getOntResource(TOP_CLASS).asClass
		val outputDirectory = new File(rootOutputDirectory, ABSTRACT_PACKAGE.replace('.', File.separatorChar))
		if (!outputDirectory.exists) {
			outputDirectory.mkdirs
		}
		val sourceFile = new File(outputDirectory, ABSTRACT_SCALA)
		val writer = new FileWriter(sourceFile)
		writer.write("package %s\n\n".format(ABSTRACT_PACKAGE))
		generateSubclassesOf(top, writer)
		writer.close
	}
  
	private def generateSubclassesOf(c: OntClass, w: Writer) {
		for (subclass <- c.listSubClasses(true);
			 if (!subclass.equals(OWL.Nothing))) {
			w.write("abstract class Abstract%s".format(subclass.getLocalName))
			if (!c.equals(TOP_CLASS)) {
				w.write(" extends Abstract%s".format(c.getLocalName))
			}
			w.write(" {\n\n")
			for (property <- subclass.listDeclaredProperties(true)) {
				val range = if (property.getRange.equals(XSD.xstring)) {
					"String"
				} else {
					"Abstract" + property.getRange.getLocalName
				}
				w.write("  def %s: List[%s]\n".format(property.getLocalName, range))
			}
			w.write("\n}\n")
			generateSubclassesOf(subclass, w)
		}
	}

	/**
	 * <p>Generate Scala source code for each "method" in a PelletServer service description.
	 * It is assumed that each knowledge-base in a service description has the same methods with the same signature.
	 * <p>Parameter-less methods looks like this:
	 * <pre>
	 *   def realize = {
	 *       mimetype: String => new Result(fetchResponse(info.`kb-services`("realize")(mimetype)) with RdfResponse with RdfXmlResponse
	 *   }
	 * </pre>
	 * while methods with parameters look like this:
	 * <pre>
	 *   def query(query: String = null, defaultGraphUri: String = null, namedGraphUri: String = null) = {
	 *       mimetype: String => new Result(fetchResponse(
	 *           info.`kb-services`("query"),
	 *           Map("query" -> query,  "default-graph-uri" -> defaultGraphUri, "named-graph-uri" -> namedGraphUri)
	 *       )(mimetype)) with SparqlXmlResponse with JsonResponse with SparqlResponse
	 * </pre>
	 **/
  
	def generateMethods(jsonServiceDescription: URL) {
		val connection = jsonServiceDescription.openConnection
		connection.setRequestProperty("Accept", "text/json")
		val json = parse(new InputStreamReader(connection.getInputStream))
		val methods = findMethods(json)
		val outputDirectory = new File(rootOutputDirectory, METHOD_PACKAGE.replace('.', File.separatorChar))
		if (!outputDirectory.exists) {
			outputDirectory.mkdirs
		}
		val sourceFile = new File(outputDirectory, KB_SCALA)
		val writer = new FileWriter(sourceFile)
		val template = Source.fromURL(getClass.getClassLoader.getResource("templates/KnowledgeBase.scala"))
		for (line <- template) {
			writer.write(line)
		}
		for ((methodName, (httpMethods, responseTypes, params)) <- methods) {
			val methodCamelCase = enCamelList(methodName.split('-').toList)
			writer.write("  def %s".format(methodCamelCase))
			if (params.length > 0) {
				writer.write("(" + params.map(param => {
					"%s: String = null".format(escapeReserved(enCamelList(param.split('-').toList)))
				}).mkString(", ") + ")")
			}
			writer.write(" = {\n")
			writer.write("      mimetype: String => new Result(fetchResponse(\n")
			writer.write("          info.`kb-services`(\"%s\")".format(methodName))
			if (params.length > 0) {
				writer.write(""",
          Map(%s)
      """.format(params.map(param => {
    	  	    	"\"%s\" -> %s".format(param, escapeReserved(enCamelList(param.split('-').toList)))
	 	  	  	}).mkString(", ")))
			}
			writer.write(")(mimetype)) with ")
			val traits = responseTypes.flatMap(r => r match {
				case "application/rdf+xml" => List("RdfXmlResponse", "RdfResponse")
				case "text/json" => List("PlainJsonResponse")
				case "application/sparql-results+xml" => List("SparqlXmlResponse", "SparqlResponse", "SparqlBooleanResponse")
				case "application/sparql-results+json" => List("SparqlJsonResponse")
				case "text/turtle" => Nil
				case "text/html" => Nil
				case r @ _ => error("Unexpected response type '%s'".format(r))
			})
			writer.write(traits.mkString(" with "))
			writer.write("\n  }\n")
		}
		writer.write("}\n")
		// Add an object with some static details about provenance
		val serverInfo = json \ "server-information"
		val JField(_, JString(sdDate)) = serverInfo \ "description-created"
		val JField(_, JString(sdVersion)) = serverInfo \ "server-version"
		val genDate = xmlDate(new Date())
		writer.write("""
object SDProvenance {
    val SD_VERSION = "%s"
    val SD_DATE = "%s"
    val GEN_DATE = "%s"
}
""".format(sdVersion, sdDate, genDate))
		writer.close
	}
  
	def generateExtractionClasses() {
		val outputDirectory = new File(rootOutputDirectory, EXTRACTION_PACKAGE.replace('.', File.separatorChar))
		if (!outputDirectory.exists) {
			outputDirectory.mkdirs
		}
		val sourceFile = new File(outputDirectory, EXTRACTION_SCALA)
		val writer = new FileWriter(sourceFile)
		val template = Source.fromURL(getClass.getClassLoader.getResource("templates/Extraction.scala"))
		for (line <- template) {
			writer.write(line)
		}
		writer.close
	}

	private def enCamelList(l: List[String]) = {
		(l.head :: l.tail.map(_.capitalize)).mkString
	}
  
	private def escapeReserved(param: String) = {
		if (RESERVED.contains(param)) {
			"`%s`".format(param)
		} else {
			param
		}
	}
	
	private def xmlDate(d: Date) = {
		val dtf = DatatypeFactory.newInstance
		val gc = new GregorianCalendar(TimeZone.getTimeZone("UTC"))
		gc.setTime(d)
		dtf.newXMLGregorianCalendar(gc).toXMLFormat
	}

	private def findMethods(json: JValue) = {
		val methods = scala.collection.mutable.Map.empty[String, (List[String], List[String], List[String])]
		for (JField("knowledge-bases", kbs) <- json;
			 JField("kb-services", services) <- kbs;
			 JField(methodName, JObject(List(JField("response-mimetype", mimetypes), JField("endpoint", endpoint)))) <- services) {
			val responseMimetypes = for (JString(mimetype) <- mimetypes) yield mimetype
			val httpMethods = for (JField("http-methods", httpMethods) <- endpoint;
								   JString(httpMethod) <- httpMethods) yield httpMethod
			    val urlTemplates = for (JField("url", JString(url)) <- endpoint) yield url
			    assert(urlTemplates.toList.length == 1)
				val params = new Regex("""\{(\?)?(.*?)\}""").findAllIn(urlTemplates.head).matchData.toList.flatMap(m => m.group(2).split(','))	 	  
				methods.get(methodName) match {
					case Some((existingHttpMethods, existingResponseMimetypes, existingParams)) =>
						assert(Set(existingHttpMethods) == Set(httpMethods))
						assert(Set(existingResponseMimetypes) == Set(responseMimetypes))
						assert(Set(existingParams) == Set(params))
					case None =>
						methods.put(methodName, (httpMethods.toList, responseMimetypes.toList, params.toList))
			    }
		}
		methods
	}
}
