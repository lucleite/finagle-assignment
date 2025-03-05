package com.lucas.marvel.api.helpers

import cats.data.Validated.{Invalid, Valid}
import cats.data.NonEmptyChain
import com.lucas.marvel.api.MarvelCharactersRequestValidated
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class RequestValidationSpec extends AnyFreeSpec with Matchers {

  "validateComicYear should validate comicYear correctly" - {
    "return valid if comicYear is None" in {
      val result = RequestValidation.validateComicYear(None)
      result shouldBe Valid(None)
    }

    "return valid if comicYear is >= 1900" in {
      val result = RequestValidation.validateComicYear(Some(2000))
      result shouldBe Valid(Some(2000))
    }

    "return invalid if comicYear is < 1900" in {
      val result = RequestValidation.validateComicYear(Some(1800))
      result shouldBe Invalid(NonEmptyChain(ComicYearIsEarlierThan1900))
    }
  }

  "validateOrderBy should validate orderBy correctly" - {
    "return valid if orderBy is None" in {
      val result = RequestValidation.validateOrderBy(None)
      result shouldBe Valid(None)
    }

    "return valid if orderBy is one of the allowed values" in {
      val result = RequestValidation.validateOrderBy(Some("title"))
      result shouldBe Valid(Some("title"))
    }

    "return invalid if orderBy is not one of the allowed values" in {
      val result = RequestValidation.validateOrderBy(Some("invalid"))
      result shouldBe Invalid(NonEmptyChain(OrderByContainsInvalidValue))
    }
  }

  "validateAllUserInputParams should validate query parameters correctly" - {
    "return valid if all parameters are allowed" in {
      val clientParams = Map("name" -> "Spider-Man", "comicYear" -> "2000")
      val allowedParams = Set("name", "comicYear", "orderBy")
      val result = RequestValidation.validateAllUserInputParams(clientParams, allowedParams)
      result shouldBe Valid(clientParams)
    }

    "return invalid if there are disallowed parameters" in {
      val clientParams = Map("name" -> "Spider-Man", "invalidParam" -> "value")
      val allowedParams = Set("name", "comicYear", "orderBy")
      val result = RequestValidation.validateAllUserInputParams(clientParams, allowedParams)
      result shouldBe Invalid(NonEmptyChain(ContainsInvalidQueryParameters))
    }
  }

  "validate should validate the entire request correctly" - {
    "return valid if all fields are valid" in {
      val clientParams = Map("name" -> "Spider-Man", "comicYear" -> "2000")
      val allowedParams = Set("name", "comicYear", "orderBy")
      val result = RequestValidation.validate("Spider-Man", Some(2000), Some("title"), clientParams, allowedParams)
      result shouldBe Valid(MarvelCharactersRequestValidated("Spider-Man", Some(2000), Some("title")))
    }

    "return invalid if any field is invalid" in {
      val clientParams = Map("name" -> "Spider-Man", "comicYear" -> "1800")
      val allowedParams = Set("name", "comicYear", "orderBy")
      val result = RequestValidation.validate("Spider-Man", Some(1800), Some("invalid"), clientParams, allowedParams)
      result shouldBe Invalid(NonEmptyChain(ComicYearIsEarlierThan1900, OrderByContainsInvalidValue))
    }
  }
}