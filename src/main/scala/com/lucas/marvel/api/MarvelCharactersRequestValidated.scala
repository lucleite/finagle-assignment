package com.lucas.marvel.api

/**
 *   - Represents a validated request for the MarvelCharactersEndpoint
 *     containing the sanitized client input to call the Marvel API.
 */
case class MarvelCharactersRequestValidated(
  name: String,
  comicYear: Option[Int],
  orderBy: Option[String]
)
