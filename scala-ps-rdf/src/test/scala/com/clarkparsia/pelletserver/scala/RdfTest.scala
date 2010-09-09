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