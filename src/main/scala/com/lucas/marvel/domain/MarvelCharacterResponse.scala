package com.lucas.marvel.domain

import com.lucas.marvel.client.response.{MarvelApiCharacterResult, MarvelApiComicsResult}
import com.typesafe.scalalogging.LazyLogging

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.{Failure, Success, Try}

case class MarvelCharacterResponseWrapper(
  status: String,
  statusCode: Int,
  errors: Option[List[String]],
  payload: Option[MarvelCharacterResponse]
)

case class MarvelCharacterResponse(
  data: List[MarvelCharacterResponseData]
)

case class MarvelCharacterResponseData(
  character: MarvelCharacter,
  comics: List[MarvelCharacterComic]
)

sealed trait ResponseStatus
case object Successful extends ResponseStatus
case object Error      extends ResponseStatus

object MarvelCharacterResponseData extends LazyLogging {

  def apply(
    characterApiResponse: MarvelApiCharacterResult,
    characterComicApiResponse: List[MarvelApiComicsResult]
  )(implicit comicDateFormatter: DateTimeFormatter): MarvelCharacterResponseData = {

    val comics = characterComicApiResponse.map(comic =>
      MarvelCharacterComic(
        title = comic.title,
        issueNumber = comic.issueNumber,
        releaseYear = extractReleaseYear(comic, comicDateFormatter)
      )
    )

    MarvelCharacterResponseData(MarvelCharacter(characterApiResponse), comics)
  }

  private def extractReleaseYear(comic: MarvelApiComicsResult, formatter: DateTimeFormatter): Option[Int] =
    comic.dates
      .find(_.`type`.equalsIgnoreCase("focDate"))
      .flatMap(date =>
        Try(LocalDate.parse(date.date, formatter)) match {
          case Success(localDate) => Some(localDate.getYear)
          case Failure(_) =>
            logger.warn(s"Failed to parse date ${date.date} for comic ${comic.title}. Release year will be skip.")
            None
        }
      )
}
