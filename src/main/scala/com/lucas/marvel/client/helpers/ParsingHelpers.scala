package com.lucas.marvel.client.helpers

import cats.effect.IO
import com.lucas.marvel.client.{JsonDecodingError, MarvelApiError}
import com.lucas.marvel.client.response.{
  ApiResponseEntity,
  ApiResponseWrapper,
  MarvelApiCharacterResult,
  MarvelApiComicsResult
}
import com.twitter.finagle.http.Response
import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.parser.decode

object ParsingHelpers {

  private[client] implicit val parseCharacterResponse
    : Response => IO[Either[MarvelApiError, List[MarvelApiCharacterResult]]] =
    response =>
      IO {
        decode[ApiResponseWrapper[MarvelApiCharacterResult]](
          response.contentString
        ).fold(
          error => Left(JsonDecodingError(error)),
          decoded => Right(decoded.data.results)
        )
      }

  private[client] implicit val parseComicsResponse
    : Response => IO[Either[MarvelApiError, List[MarvelApiComicsResult]]] =
    response =>
      IO {
        decode[ApiResponseWrapper[MarvelApiComicsResult]](
          response.contentString
        ).fold(
          error => Left(JsonDecodingError(error)),
          decoded => Right(decoded.data.results)
        )
      }

  private[client] implicit val parseCharacterResponseWrapper
    : Response => IO[Either[MarvelApiError, ApiResponseWrapper[MarvelApiCharacterResult]]] =
    getParseApiResponseWrapper[MarvelApiCharacterResult]

  private[client] implicit val parseComicsResponseWrapper
    : Response => IO[Either[MarvelApiError, ApiResponseWrapper[MarvelApiComicsResult]]] =
    getParseApiResponseWrapper[MarvelApiComicsResult]

  private def getParseApiResponseWrapper[T <: ApiResponseEntity: Decoder]
    : Response => IO[Either[MarvelApiError, ApiResponseWrapper[T]]] = response =>
    IO {
      decode[ApiResponseWrapper[T]](response.contentString)
        .fold(
          error => Left(JsonDecodingError(error)),
          decoded => Right(decoded)
        )
    }
}
