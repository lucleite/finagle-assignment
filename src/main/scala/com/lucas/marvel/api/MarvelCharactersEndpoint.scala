package com.lucas.marvel.api

import cats.data.Validated
import cats.effect.IO
import com.lucas.marvel.api.helpers.{RequestHandlers, RequestValidation}
import com.lucas.marvel.domain.MarvelCharacterResponseWrapper
import com.lucas.marvel.services.traits.MarvelCharactersService
import io.finch._
import shapeless._

/**
 * Marvel Characters API Endpoint. Endpoint: GET
 * /marvel-characters?name={name}&comicYear={year}&orderBy={order}
 *
 * Parameters:
 *   - name (required): The Marvel character's name.
 *   - comicYear (optional): Comics' year filter (defaults to last 10 years).
 *   - orderBy (optional): Sorting order (title [default], issueNumber).
 */
class MarvelCharactersEndpoint(service: MarvelCharactersService, apiVersion: String) extends Endpoint.Module[IO] {

  private val endpointPath = path(apiVersion) :: path("marvel-characters")
  private val endpointQueryParameters =
    param("name") :: paramOption[Int]("comicYear") :: paramOption[String]("orderBy")

  /**
   * Finch ignores unexpected parameters, so we need to collect all user input
   * and compare it with the allowed parameters if we want to validate the
   * parameter list.
   */

  private val allowedEndpointParameters = Set("name", "comicYear", "orderBy")
  private val allClientRequestParams    = root.map(_.params.toList.toMap)

  private def rawEndpoint = get(endpointPath :: endpointQueryParameters :: allClientRequestParams)

  def endpoint: Endpoint[IO, MarvelCharacterResponseWrapper] = rawEndpoint.mapOutputAsync {
    case name :: comicYear :: orderBy :: allClientRequestParams :: HNil =>
      RequestValidation.validate(name, comicYear, orderBy, allClientRequestParams, allowedEndpointParameters) match {
        case Validated.Valid(req)      => RequestHandlers.handleValidRequest(service, req)
        case Validated.Invalid(errors) => RequestHandlers.handleInvalidRequests(errors)
      }
  }
}
