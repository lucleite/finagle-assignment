package com.lucas.marvel.services.impl

import com.lucas.marvel.domain.{
  MarvelCharacter,
  MarvelCharacterComic,
  MarvelCharacterResponse,
  MarvelCharacterResponseData
}
import com.lucas.marvel.services.traits.CacheService
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._

class CacheServiceImplSpec extends AnyFreeSpec with Matchers {

  val testResponse = MarvelCharacterResponse(
    List(
      MarvelCharacterResponseData(
        MarvelCharacter("name", "description", "thumbnail"),
        List(MarvelCharacterComic("title", 1, Some(2020)))
      )
    )
  )

  "CacheServiceImpl" - {
    "should return None for a cache miss" in {
      val cacheService: CacheService = new CacheServiceImpl
      cacheService.get("nonexistentKey") shouldBe None
    }

    "should return the cached value for a cache hit" in {
      val cacheService: CacheService = new CacheServiceImpl
      val response                   = testResponse
      cacheService.set("spiderman", response, 1.minute)
      cacheService.get("spiderman") shouldBe Some(response)
    }

    "should return None for an expired cache entry" in {
      val cacheService: CacheService = new CacheServiceImpl
      val response                   = testResponse
      cacheService.set("spiderman", response, 1.millisecond)
      Thread.sleep(10)
      cacheService.get("spiderman") shouldBe None
    }
  }
}
