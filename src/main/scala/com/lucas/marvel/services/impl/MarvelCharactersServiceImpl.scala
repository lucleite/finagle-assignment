package com.lucas.marvel.services.impl

import cats.effect.IO
import cats.implicits._
import com.lucas.marvel.client.response.MarvelApiCharacterResult
import com.lucas.marvel.client.traits.MarvelApiClient
import com.lucas.marvel.domain.{MarvelCharacterResponse, MarvelCharacterResponseData}
import com.lucas.marvel.services.traits.{CacheService, MarvelCharactersService}
import com.typesafe.scalalogging.LazyLogging

import java.time.format.DateTimeFormatter
import scala.concurrent.duration.DurationInt

class MarvelCharactersServiceImpl(client: MarvelApiClient, cache: CacheService)
    extends MarvelCharactersService
    with LazyLogging {

  private val default_ttl = 10.minutes // 10 minutes
  implicit private val comicDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")

  override def getMarvelCharacterResponse(
    name: String,
    comicYear: Option[Int],
    orderBy: Option[String]
  ): IO[Either[Throwable, MarvelCharacterResponse]] = {

    /**
     * Using key-value pairs to reduce the likelihood of cache key collisions,
     * clearly segregating different fields.
     *
     * e.g.
     *   - name=spiderman|comicYear=2021|orderBy=title
     *   - name=spiderman|comicYear=2021|orderBy=
     *   - name=spiderman|comicYear=|orderBy=title
     */
    val cacheKey = s"name=$name|comicYear=${comicYear.getOrElse("")}|orderBy=${orderBy.getOrElse("")}"

    /**
     * Get the value from the cache if it exists, otherwise fetch from the
     * Marvel API.
     */
    IO.pure(cache.get(cacheKey))
      .flatMap {
        case Some(value) =>
          IO.pure(Right(value))
        case None =>
          logger.debug(s"Fetching from Marvel API for key: $cacheKey")
          requestFromMarvelApi(name, comicYear, orderBy, cacheKey)
      }
      .handleErrorWith { e =>
        logger.error(s"Error fetching Marvel character response: $e")
        IO.pure(Left(e))
      }
  }

  private def requestFromMarvelApi(
    name: String,
    comicYear: Option[Int],
    orderBy: Option[String],
    cacheKey: String
  ): IO[Either[Throwable, MarvelCharacterResponse]] =

    // Fetch the Marvel character details from the API
    client.fetchMarvelCharacter(name).flatMap {
      case Left(error)             => IO.pure(Left(error)) // Return the error if character fetch fails
      case Right(charactersResult) =>

        // Fetch comics for each character and construct the response
        fetchComicsForCharacters(charactersResult, comicYear, orderBy).map { characterResponses =>
          val response = MarvelCharacterResponse(characterResponses)

          // Cache the response before returning
          cache.set(cacheKey, response, default_ttl)
          Right(response)
        }.handleErrorWith { e =>
          logger.error(s"Error fetching Marvel character response: $e")
          IO.pure(Left(e))
        }
    }

  /**
   * Fetches comics for each character in the given list. If fetching comics
   * fails, an empty list is returned for that character.
   */
  private def fetchComicsForCharacters(
    characters: List[MarvelApiCharacterResult],
    comicYear: Option[Int],
    orderBy: Option[String]
  ): IO[List[MarvelCharacterResponseData]] =
    characters.traverse { char =>
      client.fetchMarvelCharacterComics(char.id, comicYear, orderBy).flatMap {
        case Left(_) =>
          logger.warn(s"Error fetching comics for character: ${char.id}. Returning an empty list.")
          IO.pure(MarvelCharacterResponseData(char, List.empty))
        case Right(comics) =>
          IO.pure(MarvelCharacterResponseData(char, comics))
      }
    }
  /**/
}
