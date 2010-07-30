package com.clarkparsia.pelletserver.scala.api

import com.clarkparsia.pelletserver.scala._

trait DefaultService {
	protected val info: JService
	def hasUriTemplate = List(info.endpoint.url)
	def hasResponseMimeType = info.`response-mimetype`
}

class Service(override val info: JService) extends AbstractService with DefaultService
