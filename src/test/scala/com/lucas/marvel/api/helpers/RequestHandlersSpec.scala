package com.lucas.marvel.api.helpers

import cats.data.NonEmptyChain
import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import com.lucas.marvel.api.MarvelCharactersRequestValidated
import com.lucas.marvel.client.MarvelApiHttpError
import com.lucas.marvel.domain.{MarvelCharacterResponse, MarvelCharacterResponseWrapper, Successful}
import com.lucas.marvel.services.traits.MarvelCharactersService
import com.twitter.finagle.http.Status
import io.finch.Output
import org.mockito.Mockito._
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.OptionValues
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.ExecutionContext

class RequestHandlersSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with MockitoSugar with OptionValues {

  implicit val ec: ExecutionContext = ExecutionContext.global

  "handleInvalidRequests should return a BadRequest response with error messages" in {
    val errors                                             = NonEmptyChain(ComicYearIsEarlierThan1900, OrderByContainsInvalidValue)
    val result: IO[Output[MarvelCharacterResponseWrapper]] = RequestHandlers.handleInvalidRequests(errors)

    result.asserting { output =>
      output.status shouldBe Status.BadRequest
      output.value.status shouldBe "Error"
      output.value.statusCode shouldBe Status.BadRequest.code
      output.value.errors shouldBe Some(
        List(
          "If provided, 'comicYear' must be equal or greater than 1900., If provided, 'orderBy' must be one of 'title' or 'issueNumber'."
        )
      )
    }
  }

  "handleValidRequest should return character data with 200 OK if successful" in {
    val service           = mock[MarvelCharactersService]
    val request           = MarvelCharactersRequestValidated("Spider-Man", Some(2000), Some("title"))
    val characterResponse = mock[MarvelCharacterResponse]

    when(service.getMarvelCharacterResponse("Spider-Man", Some(2000), Some("title")))
      .thenReturn(IO.pure(Right(characterResponse)))

    val result: IO[Output[MarvelCharacterResponseWrapper]] = RequestHandlers.handleValidRequest(service, request)

    result.asserting { output =>
      output.status shouldBe Status.Ok
      output.value.status shouldBe Successful.toString
      output.value.statusCode shouldBe Status.Ok.code
      output.value.payload shouldBe Some(characterResponse)
    }
  }

  "it should propagate Marvel API errors with their specific codes and messages" in {
    val service  = mock[MarvelCharactersService]
    val request  = MarvelCharactersRequestValidated("Spider-Man", Some(2000), Some("title"))
    val apiError = MarvelApiHttpError(401, "Invalid API key")

    when(service.getMarvelCharacterResponse("Spider-Man", Some(2000), Some("title")))
      .thenReturn(IO.pure(Left(apiError)))

    val result: IO[Output[MarvelCharacterResponseWrapper]] = RequestHandlers.handleValidRequest(service, request)
    result.asserting { output =>
      output.status shouldBe Status.Unauthorized
      output.value.status shouldBe "Error"
      output.value.statusCode shouldBe 401
      output.value.errors shouldBe Some(List("Invalid API key"))
    }
  }

  "it should return a generic 500 Internal Server Error for unexpected errors" in {
    val service         = mock[MarvelCharactersService]
    val request         = MarvelCharactersRequestValidated("Spider-Man", Some(2000), Some("title"))
    val unexpectedError = new RuntimeException("Unexpected error")

    when(service.getMarvelCharacterResponse("Spider-Man", Some(2000), Some("title")))
      .thenReturn(IO.pure(Left(unexpectedError)))

    val result: IO[Output[MarvelCharacterResponseWrapper]] = RequestHandlers.handleValidRequest(service, request)

    result.asserting { output =>
      output.status shouldBe Status.InternalServerError
      output.value.status shouldBe "Error"
      output.value.statusCode shouldBe Status.InternalServerError.code
      output.value.errors shouldBe Some(List("Error retrieving Marvel character data"))
    }
  }
}
