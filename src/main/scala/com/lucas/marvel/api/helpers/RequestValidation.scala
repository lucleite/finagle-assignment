package com.lucas.marvel.api.helpers

import cats.data.ValidatedNec
import cats.syntax.all._
import com.lucas.marvel.api.MarvelCharactersRequestValidated

sealed trait RequestValidation {
  def errorMessage: String
}
case object ComicYearIsEarlierThan1900 extends RequestValidation {
  def errorMessage: String = "If provided, 'comicYear' must be equal or greater than 1900."
}
case object OrderByContainsInvalidValue extends RequestValidation {
  def errorMessage: String = "If provided, 'orderBy' must be one of 'title' or 'issueNumber'."
}
case object ContainsInvalidQueryParameters extends RequestValidation {
  def errorMessage: String =
    "The request contain other query parameters than the allowed: 'name', 'comicYear', 'orderBy'."
}

/**
 * Responsible for validating and converting a raw MarvelCharactersRequest into
 * a validated MarvelCharactersValidatedRequest.
 *
 * The validation process includes:
 *   - Ensuring the comicYear is equal or greater than 1900, or empty.
 *   - Ensuring the orderBy field contains valid, allowed values ("title",
 *     "issueNumber") or empty.
 *   - Ensuring the request contains only the valid query parameters.
 *
 * Use of Cats.validated due to suggestion here:
 *   - https://github.com/finagle/finch/pull/1471
 */
object RequestValidation {

  type ValidationResult[A] = ValidatedNec[RequestValidation, A]

  def validate(
    name: String,
    comicYear: Option[Int],
    orderBy: Option[String],
    clientRequestParams: Map[String, String],
    allowedEndpointParams: Set[String]
  ): ValidationResult[MarvelCharactersRequestValidated] =

    (
      validateComicYear(comicYear),
      validateOrderBy(orderBy),
      validateAllUserInputParams(clientRequestParams, allowedEndpointParams)
    ).mapN { case (validComicYear, validOrderBy, _) =>
      MarvelCharactersRequestValidated(name, validComicYear, validOrderBy)
    }

  private[helpers] def validateOrderBy(
    orderBy: Option[String],
    allowedOrderByValues: Set[String] = Set("title", "issueNumber")
  ): ValidationResult[Option[String]] =
    orderBy match {
      case None => None.validNec
      case Some(order) =>
        if (allowedOrderByValues.contains(order)) Some(order).validNec
        else OrderByContainsInvalidValue.invalidNec
    }

  private[helpers] def validateComicYear(
    comicYear: Option[Int]
  ): ValidationResult[Option[Int]] =
    comicYear match {
      case None => None.validNec
      case Some(year) =>
        if (year >= 1900) Some(year).validNec
        else ComicYearIsEarlierThan1900.invalidNec
    }

  // Validate if the request contains only the valid query parameters
  private[helpers]  def validateAllUserInputParams(
    clientRequestParams: Map[String, String],
    allowedEndpointParams: Set[String]
  ): ValidationResult[Map[String, String]] = {
    val invalidParams = clientRequestParams.keySet -- allowedEndpointParams
    if (invalidParams.isEmpty) clientRequestParams.validNec
    else ContainsInvalidQueryParameters.invalidNec
  }

}
