package com.lucas.marvel.client.helpers

import cats.data.EitherT
import cats.effect.IO
import com.lucas.marvel.client.MarvelApiError
import com.lucas.marvel.client.response.{ApiResponseEntity, ApiResponseWrapper}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.Service
import com.typesafe.scalalogging.LazyLogging

object PaginationHelpers extends LazyLogging {

  // The maximum limit for the Marvel API is 100
  private val defaultPageLimitSize = 50

  /**
   * Entry point function to start fetching with pagination with an empty list
   * and offset = 0
   */
  def fetchAllPages[T <: ApiResponseEntity](url: String)(implicit
    parseResponseWrapper: Response => IO[Either[MarvelApiError, ApiResponseWrapper[T]]],
    parseEntityResponse: Response => IO[Either[MarvelApiError, List[T]]],
    client: Service[Request, Response]
  ): IO[Either[MarvelApiError, List[T]]] =
    fetchAllPagesRecursive[T](url, entitiesAcc = List.empty, currentOffset = 0)

  private def fetchAllPagesRecursive[T <: ApiResponseEntity](
    url: String,
    entitiesAcc: EntitiesAcc[T],
    currentOffset: Int
  )(implicit
    parseResponseWrapper: Response => IO[Either[MarvelApiError, ApiResponseWrapper[T]]],
    parseEntityResponse: Response => IO[Either[MarvelApiError, List[T]]],
    client: Service[Request, Response]
  ): IO[Either[MarvelApiError, List[T]]] =

    fetchPage(url, entitiesAcc, currentOffset).flatMap {
      case Left(error) => IO.pure(Left(error))
      case Right((entities, nextPageOffset, totalEntitiesToFetch)) =>
        if (nextPageOffset >= totalEntitiesToFetch) {
          // We are done fetching
          logger.debug(s"Finished fetching all pages for url: $url")
          IO.pure(Right(entities))
        } else {
          // Keep fetching the rest of the pages
          fetchAllPagesRecursive(url, entities, nextPageOffset)
        }
    }

  private type TotalEntitiesToFetch                = Int
  private type NextPageOffset                      = Int
  private type EntitiesAcc[T <: ApiResponseEntity] = List[T]

  private def fetchPage[T <: ApiResponseEntity](
    url: String,
    entitiesAcc: EntitiesAcc[T],
    pageOffset: Int
  )(implicit
    parseResponseWrapper: Response => IO[Either[MarvelApiError, ApiResponseWrapper[T]]],
    parseEntityResponse: Response => IO[Either[MarvelApiError, List[T]]],
    client: Service[Request, Response]
  ): IO[Either[MarvelApiError, (EntitiesAcc[T], NextPageOffset, TotalEntitiesToFetch)]] = {

    logger.debug(s"Fetching page with offset= $pageOffset")
    val urlWithPaginationParameters = s"$url&offset=$pageOffset&limit=$defaultPageLimitSize"
    (
      for {
        response            <- EitherT(RequestHelpers.sendRequest(urlWithPaginationParameters))
        responseWrapper     <- EitherT(parseResponseWrapper(response))
        parsed              <- EitherT(parseEntityResponse(response))
        accumulatedEntities  = entitiesAcc ::: parsed
        nextPageOffset       = responseWrapper.data.offset + defaultPageLimitSize
        totalEntitiesToFetch = responseWrapper.data.total
      } yield {
        logger.debug(
          s"Finished fetching page with offset= $pageOffset, accumulatedEntitiesSize= ${accumulatedEntities.size}, nextPageOffset= $nextPageOffset, totalEntitiesToFetch= $totalEntitiesToFetch"
        )
        (accumulatedEntities, nextPageOffset, totalEntitiesToFetch)
      }
    ).value
  }
}
