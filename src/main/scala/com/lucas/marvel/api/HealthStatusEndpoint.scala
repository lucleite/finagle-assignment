package com.lucas.marvel.api

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import io.finch._

class HealthStatusEndpoint(apiVersion: String) extends Endpoint.Module[IO] with LazyLogging {

  val endpointPath = path(apiVersion) :: path("health")

  val endpoint = get(endpointPath) {
    Ok("Service is up and running")
  }.handle { case e: Exception =>
    logger.error(s"Failed to retrieve health status. Error: $e")
    InternalServerError(new Exception("Failed to retrieve health status"))
  }
}
