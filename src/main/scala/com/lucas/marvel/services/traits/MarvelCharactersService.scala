package com.lucas.marvel.services.traits

import cats.effect.IO
import com.lucas.marvel.domain.MarvelCharacterResponse

trait MarvelCharactersService {

  def getMarvelCharacterResponse(
    name: String,
    comicYear: Option[Int],
    orderBy: Option[String]
  ): IO[Either[Throwable, MarvelCharacterResponse]]
}
