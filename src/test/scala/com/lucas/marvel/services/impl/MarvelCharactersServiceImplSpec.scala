package com.lucas.marvel.services.impl

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.lucas.marvel.client.response.{ComicDate, MarvelApiCharacterResult, MarvelApiComicsResult, Thumbnail}
import com.lucas.marvel.client.traits.MarvelApiClient
import com.lucas.marvel.domain.{MarvelCharacterResponse, MarvelCharacterResponseData}
import com.lucas.marvel.services.traits.CacheService
import org.mockito.Mockito._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar

import java.time.format.DateTimeFormatter
import scala.concurrent.duration._

class MarvelCharactersServiceImplSpec extends AnyFreeSpec with Matchers with MockitoSugar {

  implicit private val comicDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")

  "MarvelCharactersServiceImpl" - {
    "should fetch from API if cache is empty and cache the result" in {
      val client  = mock[MarvelApiClient]
      val cache   = mock[CacheService]
      val service = new MarvelCharactersServiceImpl(client, cache)

      val name      = "Spider-Man"
      val comicYear = Some(2000)
      val orderBy   = Some("title")
      val cacheKey  = s"name=$name|comicYear=${comicYear.getOrElse("")}|orderBy=${orderBy.getOrElse("")}"

      val characterResult =
        MarvelApiCharacterResult(1, "Spider-Man", "A superhero in New York", Thumbnail("path", ".ext"))
      val comicResult = MarvelApiComicsResult("Comic Title", 1, List(ComicDate("type", "date")))
      val expectedResponse =
        MarvelCharacterResponse(List(MarvelCharacterResponseData(characterResult, List(comicResult))))

      when(cache.get(cacheKey)).thenReturn(None)
      when(client.fetchMarvelCharacter(name)).thenReturn(IO.pure(Right(List(characterResult))))
      when(client.fetchMarvelCharacterComics(characterResult.id, comicYear, orderBy))
        .thenReturn(IO.pure(Right(List(comicResult))))

      val result = service.getMarvelCharacterResponse(name, comicYear, orderBy).unsafeRunSync()

      result shouldBe Right(expectedResponse)
      verify(cache).set(cacheKey, expectedResponse, 10.minutes)
    }

    "should fetch from cache if available" in {
      val client  = mock[MarvelApiClient]
      val cache   = mock[CacheService]
      val service = new MarvelCharactersServiceImpl(client, cache)

      val name      = "Spider-Man"
      val comicYear = Some(2000)
      val orderBy   = Some("title")
      val cacheKey  = s"name=$name|comicYear=${comicYear.getOrElse("")}|orderBy=${orderBy.getOrElse("")}"

      val characterResult =
        MarvelApiCharacterResult(1, "Spider-Man", "A superhero in New York", Thumbnail("path", ".ext"))
      val comicResult = MarvelApiComicsResult("Comic Title", 1, List(ComicDate("type", "date")))
      val expectedResponse =
        MarvelCharacterResponse(List(MarvelCharacterResponseData(characterResult, List(comicResult))))

      when(cache.get(cacheKey)).thenReturn(Some(expectedResponse))

      val result = service.getMarvelCharacterResponse(name, comicYear, orderBy).unsafeRunSync()

      result shouldBe Right(expectedResponse)
      verify(client, never()).fetchMarvelCharacter(name)
      verify(client, never()).fetchMarvelCharacterComics(characterResult.id, comicYear, orderBy)
    }
  }
}
