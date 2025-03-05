package com.lucas.marvel.api.swagger

import cats.effect.IO
import com.twitter.finagle.http.Response
import com.typesafe.scalalogging.LazyLogging
import io.finch._

/**
 * Swagger Documentation API Endpoint. Endpoint: GET /swagger.json
 *
 * Serves the OpenAPI (Swagger) documentation for the Marvel API.
 */
class SwaggerDocsEndpoint(jsonContent: String, apiVersion: String) extends Endpoint.Module[IO] with LazyLogging {

  val endpointPath = path(apiVersion) :: path("swagger.json")

  def endpoint: Endpoint[IO, Response] = get(endpointPath).mapOutputAsync { _ =>
    IO(buildSuccessfulResponse(jsonContent))
      .handleErrorWith(_ => IO(buildErrorResponse))
  }

  private def buildSuccessfulResponse(jsonContent: String): Output[Response] = {
    val response = Response()
    response.contentString = jsonContent
    response.contentType = "application/json"
    response.statusCode = 200
    Output.payload(response)
  }

  private def buildErrorResponse: Output[Response] = {
    val errorResponse = Response()
    errorResponse.contentString = "Error retrieving the Open API JSON specification"
    errorResponse.statusCode = 500
    Output.payload(errorResponse)
  }
}
