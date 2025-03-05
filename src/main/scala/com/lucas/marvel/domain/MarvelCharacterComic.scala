package com.lucas.marvel.domain

import com.lucas.marvel.client.response.MarvelApiComicsResult

case class MarvelCharacterComic(
  title: String,
  issueNumber: Int,
  releaseYear: Option[Int]
)

object MarvelCharacterComic {

  def apply(apiResponse: MarvelApiComicsResult): MarvelCharacterComic = ???
}
