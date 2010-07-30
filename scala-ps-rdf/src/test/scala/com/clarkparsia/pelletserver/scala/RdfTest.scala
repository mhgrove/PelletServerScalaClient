package com.clarkparsia.pelletserver.scala

import org.specs._
import com.clarkparsia.pelletserver.scala.results.RdfResult._

class RdfTest extends SpecificationWithJUnit {

	val SD_JSON =  getClass.getClassLoader.getResource("root-sd.json")
	
	"scala-ps-rdf" should {
		"get results as Jena models" in {
			val kb = PelletServer(SD_JSON).getKBs()(0);
			val classify = kb.classify asRdf;
			classify.size must notBe(0)
		}
	}
	
}