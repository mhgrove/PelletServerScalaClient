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

package com.clarkparsia.pelletserver.scala.api

import com.clarkparsia.pelletserver.scala.api.schema.AbstractService
import com.clarkparsia.pelletserver.scala.api.json.JService

trait DefaultService {
	protected val info: JService
	def hasUriTemplate = List(info.endpoint.url)
	def hasResponseMimeType = info.`response-mimetype`
}

class Service(override val info: JService) extends AbstractService with DefaultService
