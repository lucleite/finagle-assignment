package com.lucas.marvel.client.traits

import cats.effect.IO
import com.lucas.marvel.client.response.{MarvelApiCharacterResult, MarvelApiComicsResult}

trait MarvelApiClient {

  def fetchMarvelCharacter(name: String): IO[Either[Throwable, List[MarvelApiCharacterResult]]]

  def fetchMarvelCharacterComics(
    id: Int,
    startYear: Option[Int],
    orderBy: Option[String]
  ): IO[Either[Throwable, List[MarvelApiComicsResult]]]
}
