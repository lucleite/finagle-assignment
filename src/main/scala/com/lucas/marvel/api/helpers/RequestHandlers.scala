package com.lucas.marvel.api.helpers

import cats.data.NonEmptyChain
import cats.effect.IO
import cats.implicits._
import com.lucas.marvel.api.MarvelCharactersRequestValidated
import com.lucas.marvel.client.MarvelApiHttpError
import com.lucas.marvel.domain.{Error, MarvelCharacterResponseWrapper, Successful}
import com.lucas.marvel.services.traits.MarvelCharactersService
import com.twitter.finagle.http.Status
import com.typesafe.scalalogging.LazyLogging
import io.finch._

object RequestHandlers extends LazyLogging {

  private val unexpectedErrorMessage = "Error retrieving Marvel character data"

  /**
   * Handles invalid requests by collecting the validation error messages and
   * returning a Bad Request response.
   */
  private[api] def handleInvalidRequests(
    errors: NonEmptyChain[RequestValidation]
  ): IO[Output[MarvelCharacterResponseWrapper]] = {
    val errorMessages = errors.toList.map(_.errorMessage).mkString(", ")
    IO.pure(buildErrorResponse(Status.BadRequest.code, errorMessages, Status.BadRequest))
  }

  /**
   * Handles a valid request by calling the MarvelCharactersService and
   * processing the response.
   *
   *   - Returns character data with 200 OK if successful.
   *   - Propagates Marvel API errors (e.g., invalid API key, quota exceeded)
   *     with their specific codes and messages.
   *   - Logs unexpected errors and returns a generic 500 Internal Server Error
   *     to avoid exposing internal details.
   */
  private[api] def handleValidRequest(
    service: MarvelCharactersService,
    req: MarvelCharactersRequestValidated
  ): IO[Output[MarvelCharacterResponseWrapper]] =
    service.getMarvelCharacterResponse(req.name, req.comicYear, req.orderBy).map {
      case Right(characterResponse) =>
        Ok(
          MarvelCharacterResponseWrapper(
            Successful.toString,
            Status.Ok.code,
            None,
            Some(characterResponse)
          )
        )
      case Left(error: MarvelApiHttpError) =>
        logger.error(s"Failed to retrieve Marvel character data: ${error.code}: ${error.message}")
        buildErrorResponse(error.code, error.message, Status.fromCode(error.code))
      case Left(error: Throwable) =>
        logger.error(s"Unexpected error: ${error.getMessage}")
        buildErrorResponse(Status.InternalServerError.code, unexpectedErrorMessage, Status.InternalServerError)
    }

  private def buildErrorResponse(code: Int, message: String, status: Status): Output[MarvelCharacterResponseWrapper] =
    Output.payload(
      MarvelCharacterResponseWrapper(
        Error.toString,
        code,
        Some(List(message)),
        None
      ),
      status
    )
}
