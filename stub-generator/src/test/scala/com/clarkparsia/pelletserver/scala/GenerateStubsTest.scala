package com.clarkparsia.pelletserver.scala

import org.specs._
import java.io.File
import scala.tools.nsc.{Interpreter,Settings,InterpreterResults}
import scala.tools.nsc.io.PlainFile
import scala.tools.nsc.util.BatchSourceFile
import scala.io.Source

class GenerateStubsTest extends SpecificationWithJUnit {

	/**
	 * Utility class to test compilation of generated sources.
	 */
	class Compiler {
		private def jarPath(c: String*) = {
			val FilePath = "jar:file:([^!]*)!/.*".r // Regexp to match jar:file:/blah/blah/something.jar!/blah.class
			for (cl <- c) yield {
				val pathToClass = "/" + cl.replace('.', '/') + ".class"
				val FilePath(path) = getClass.getResource(pathToClass).toString
				path
			}
		}
		private val systemJars = jarPath("scala.Array", "java.lang.Object")
		private val extraJars = jarPath("dispatch.Http", "org.apache.http.client.HttpClient", "org.apache.http.HttpHost")
		private val settings = new Settings(str => println(str))
		settings.bootclasspath.value_$eq(systemJars.mkString(":"))
		settings.classpath.value_=(extraJars.mkString(":"))
		private val i = new Interpreter(settings)

		def compileFile(file: File) = {
			val source = PlainFile.fromPath(file)
			i.compileSources(new BatchSourceFile(source, source.toCharArray))
		}
	}	

	// local copy of http://ps.clarkparsia.com/schema/pellet-server-schema (application/rdf+xml)
	private val SCHEMA_RDF = getClass.getClassLoader.getResource("schemas/ps.rdf")
	// local version of http://ps.clarkparsia.com (text/json)
	private val SD_JSON = getClass.getClassLoader.getResource("root-sd.json")

	/**
	 * Utility method to set up a temporary directory, execute a block, then
	 * remote the directory and its contents.
	 */
	def usingTempDir(block: File => Unit) {
		val tempDir = File.createTempFile("test", null)
		tempDir.delete
		tempDir.mkdir
		block(tempDir)
		def delete(d: File): Boolean = {
			for (f <- d.listFiles) {
				if (f.isDirectory) {
					delete(f) mustBe true
				} else {
					f.delete mustBe true
				}
			}
			d.delete
		}
		delete(tempDir) mustBe true
	}
  
	"Stubs generator" should {
		"Load PelletServer RDF schema" in {
			StubGenerator.loadSchema(SCHEMA_RDF)
			StubGenerator.getSchemaVersion must beLike {
				case Some("0.5") => true
			}
		}
		"Generate abstract source files based on schema" in {
			StubGenerator.loadSchema(SCHEMA_RDF)
			usingTempDir { tempDir =>
				StubGenerator.setOutputDirectory(tempDir)
				StubGenerator.generateAbstractHierarchy
				tempDir.listFiles must have size(1)
				new Compiler().compileFile(tempDir.listFiles()(0)) mustBe true
			}
		}
		"Generalize implementation based on service description" in {
			StubGenerator.loadSchema(SCHEMA_RDF)
			usingTempDir { tempDir =>
				StubGenerator.setOutputDirectory(tempDir)
				StubGenerator.generateMethods(SD_JSON)
				tempDir.listFiles must have size(1)
				// also need the hierarchy generated and compiled
				StubGenerator.generateAbstractHierarchy
				tempDir.listFiles must have size(2)
				// and the case classes for JSON extraction
				StubGenerator.generateExtractionClasses
				tempDir.listFiles must have size(3)
				val compiler = new Compiler()
				// compile sources in one session so they pick up dependent definitions
				compiler.compileFile(new File(tempDir, StubGenerator.ABSTRACT_SCALA)) mustBe true
				compiler.compileFile(new File(tempDir, StubGenerator.EXTRACTION_SCALA)) mustBe true
				compiler.compileFile(new File(tempDir, StubGenerator.KB_SCALA)) mustBe true
			}
		}
	}  
}
