package com.lucas.marvel.client.response

/**
 * Used to provide a context bound for the type parameter T in
 * [[ApiResponseWrapper]], adding type safety to generic construction.
 */
sealed trait ApiResponseEntity

/**
 * Represents the (needed fields) from the character entity in the Marvel API
 * More info: https://developer.marvel.com/documentation/entity_types
 */
case class MarvelApiCharacterResult(
  id: Int,
  name: String,
  description: String,
  thumbnail: Thumbnail
) extends ApiResponseEntity

case class Thumbnail(
  path: String,
  extension: String
)

/**
 * Represents the (needed fields) from the comics entity in the Marvel API More
 * info: More info: https://developer.marvel.com/documentation/entity_types
 */
case class MarvelApiComicsResult(
  title: String,
  issueNumber: Int,
  dates: List[ComicDate] // release year!!? how? dates?
) extends ApiResponseEntity

case class ComicDate(`type`: String, date: String)
