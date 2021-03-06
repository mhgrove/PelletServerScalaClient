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

package com.clarkparsia.pelletserver.scala

import org.specs._
//import net.liftweb.json.JsonParser.parse
import dispatch.json.JsValue
import java.io.InputStreamReader
import com.clarkparsia.pelletserver.scala.results.PlainJsonResult._
import com.clarkparsia.pelletserver.scala.results.SparqlBooleanResult._
import com.clarkparsia.pelletserver.scala.results.RdfXmlResult._
import com.clarkparsia.pelletserver.scala.results.SparqlXmlResult._
import com.clarkparsia.pelletserver.scala.results.SparqlResult._
import dispatch.StatusCode
import com.clarkparsia.pelletserver.scala.api.json.JServiceDescription
import scala.math.Ordering._
import dispatch.json.Js._

class FactoryTest extends SpecificationWithJUnit {

	val SD_JSON =  getClass.getClassLoader.getResource("root-sd.json")
	implicit val formats = SDFormats
	
	"Server factory object" should {
		"parse JSON" in {
			val json = JsValue.fromStream(SD_JSON.openStream)
			val serviceDescription = CustomExtractor.extractServiceDescription(json)
			serviceDescription.`server-information`.`server-version` must_== "0.9.2"
		}
		"handle live ps.clarkparsia.com" in {
			val ps = PelletServer("http://ps.clarkparsia.com")
			// Iterable(String).gt will do string comparison on two lists of strings and should
			// be the same ordering logic as in Semantic Versioning.
			Iterable(String).gt(ps.getVersion.split('.'), "0.9.2".split('.')) mustBe false
		}
		"generate KBs given service description in JSON" in {
			val kbs = PelletServer(SD_JSON).getKBs
			kbs must have size(3)
		}
		"offer parameterless services on a KB" in {
			val kb = PelletServer(SD_JSON).getKBs()(0);
			val realize = kb.realize asRdfXml;
			// realize must \( "rdf:Description" )
			val nsService = kb.nsService asJson;
			('owl ! str)(nsService) mustEqual "http://www.w3.org/2002/07/owl#" 
			(kb.consistency asBoolean) must_== true;
			val kbDiscovery = kb.kbDiscovery asRdfXml;
			(kb.explainInconsistent asRdfXml) must throwA[StatusCode] /* like {
				case StatusCode(400, msg) => true
			} */
		}
		"offer services taking parameters on a KB" in {
			val kb = PelletServer(SD_JSON).getKBs()(2);
			val query = kb.query("SELECT DISTINCT ?c WHERE {[] a ?c}") asSparqlXml;
			val search = kb.search("dog") asJson;
			list(search).size must notBe(0)
		}
		"deserialize SPARQL results" in {
			val kb = PelletServer(SD_JSON).getKBs()(1)
			val results = kb.query("""
PREFIX owl: <http://www.w3.org/2002/07/owl#> 
PREFIX sdl: <http://pellet.owldl.com/ns/sdle#>

SELECT DISTINCT ?c WHERE {
  ?c sdl:directSubClassOf owl:Thing
}""") asSparql;
			results.size must notBe(0)
		}
    }
}
