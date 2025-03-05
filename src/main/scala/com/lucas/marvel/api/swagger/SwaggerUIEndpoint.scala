package com.lucas.marvel.api.swagger

import cats.effect.IO
import com.twitter.finagle.http.Response
import io.finch._

import java.nio.charset.StandardCharsets

/**
 * Serves Swagger UI static files.
 *
 *   - GET /v1/swagger-ui/ -> Serves index.html
 *   - GET /v1/swagger-ui/{file} -> Serves other UI assets (CSS, JS, images)
 */
class SwaggerUIEndpoint(apiVersion: String) extends Endpoint.Module[IO] {

  private val endpointPath  = path(apiVersion) :: path("swagger-ui")
  private val baseFilesPath = "/swagger-ui"

  def endpoints = allFilesEndpoint :+: rootRedirectToIndexHtml

  /** Used to serve any of the static Swagger UI files */
  private def allFilesEndpoint: Endpoint[IO, Response] =
    get(endpointPath :: path[String]) { file: String => serveFile(file) }

  /** Serves index.html when accessing `/v1/swagger-ui/` */
  private def rootRedirectToIndexHtml: Endpoint[IO, Response] =
    get(endpointPath)(serveFile("index.html"))

  /** Handles serving static files */
  private def serveFile(file: String): IO[Output[Response]] = IO {
    Option(getClass.getResourceAsStream(s"$baseFilesPath/$file")) match {
      case Some(stream) =>
        val content = new String(stream.readAllBytes(), StandardCharsets.UTF_8)
        buildSuccessfulResponse(content, getMimeType(file))
      case None =>
        errorResponse(s"File not found: $file")
    }
  }

  /** Builds a successful response with content */
  private def buildSuccessfulResponse(content: String, contentType: String): Output[Response] = {
    val response = Response()
    response.contentString = content
    response.contentType = contentType
    response.statusCode = 200
    Output.payload(response)
  }

  /** Builds an error response */
  private def errorResponse(message: String): Output[Response] = {
    val response = Response()
    response.contentString = message
    response.statusCode = 500
    Output.payload(response)
  }

  /** Determines the MIME type of a file */
  private def getMimeType(file: String): String = file match {
    case f if f.endsWith(".html") => "text/html"
    case f if f.endsWith(".css")  => "text/css"
    case f if f.endsWith(".js")   => "application/javascript"
    case f if f.endsWith(".png")  => "image/png"
    case f if f.endsWith(".svg")  => "image/svg+xml"
    case _                        => "application/octet-stream"
  }
}
