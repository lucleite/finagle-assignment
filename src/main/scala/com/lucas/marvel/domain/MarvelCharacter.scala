package com.lucas.marvel.domain

import com.lucas.marvel.client.response.MarvelApiCharacterResult

case class MarvelCharacter(
  name: String,
  description: String,
  thumbnail: String
)

object MarvelCharacter {

  def apply(apiResponse: MarvelApiCharacterResult): MarvelCharacter =
    MarvelCharacter(
      name = apiResponse.name,
      description = apiResponse.description,
      thumbnail = s"${apiResponse.thumbnail.path}.${apiResponse.thumbnail.extension}"
    )
}
