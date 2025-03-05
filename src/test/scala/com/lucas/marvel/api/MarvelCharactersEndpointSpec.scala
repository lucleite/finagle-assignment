package com.lucas.marvel.api

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import com.lucas.marvel.domain.MarvelCharacterResponse
import com.lucas.marvel.services.traits.MarvelCharactersService
import com.twitter.finagle.http.Status
import io.finch.Input
import org.mockito.Mockito._
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.OptionValues
import org.scalatestplus.mockito.MockitoSugar

class MarvelCharactersEndpointSpec
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with MockitoSugar
    with OptionValues {

  val service: MarvelCharactersService = mock[MarvelCharactersService]
  val endpoint                         = new MarvelCharactersEndpoint(service, "v1").endpoint

  "MarvelCharactersEndpoint" - {
    "should return 200 OK for valid request" in {
      val input    = Input.get("/v1/marvel-characters?name=Spider-Man&comicYear=2000&orderBy=title")
      val response = mock[MarvelCharacterResponse]

      when(service.getMarvelCharacterResponse("Spider-Man", Some(2000), Some("title")))
        .thenReturn(IO.pure(Right(response)))

      endpoint(input).output.value.asserting { output =>
        output.status shouldBe Status.Ok
        output.value.payload.value shouldBe response
      }
    }

    "should return 400 Bad Request for invalid comicYear" in {
      val input = Input.get("/v1/marvel-characters?name=Spider-Man&comicYear=1800&orderBy=title")

      endpoint(input).output.value.asserting { output =>
        output.status shouldBe Status.BadRequest
        output.value.errors.value should contain("If provided, 'comicYear' must be equal or greater than 1900.")
      }
    }

    "should return 400 Bad Request for invalid orderBy" in {
      val input = Input.get("/v1/marvel-characters?name=Spider-Man&comicYear=2000&orderBy=invalid")

      endpoint(input).output.value.asserting { output =>
        output.status shouldBe Status.BadRequest
        output.value.errors.value should contain("If provided, 'orderBy' must be one of 'title' or 'issueNumber'.")
      }
    }

    "should return 400 Bad Request for invalid query parameters" in {
      val input = Input.get("/v1/marvel-characters?name=Spider-Man&invalidParam=value")

      endpoint(input).output.value.asserting { output =>
        output.status shouldBe Status.BadRequest
        output.value.errors.value should contain(
          "The request contain other query parameters than the allowed: 'name', 'comicYear', 'orderBy'."
        )
      }
    }

    "should return 500 Internal Server Error for unexpected errors" in {
      val input = Input.get("/v1/marvel-characters?name=Spider-Man&comicYear=2000&orderBy=title")

      when(service.getMarvelCharacterResponse("Spider-Man", Some(2000), Some("title")))
        .thenReturn(IO(Left(new RuntimeException("Unexpected error"))))

      endpoint(input).output.value.asserting { output =>
        output.status shouldBe Status.InternalServerError
        output.value.errors.value should contain("Error retrieving Marvel character data")
      }
    }
  }
}
