package com.lucas.marvel.client.impl

import cats.effect.IO
import com.lucas.marvel.client.MarvelApiError
import com.lucas.marvel.client.helpers.{PaginationHelpers, RequestHelpers}
import com.lucas.marvel.client.helpers.ParsingHelpers._
import com.lucas.marvel.client.response.{MarvelApiCharacterResult, MarvelApiComicsResult}
import com.lucas.marvel.client.traits.MarvelApiClient
import com.lucas.utils.AppConfig
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Request, Response}

class MarvelApiClientImpl(config: AppConfig) extends MarvelApiClient {

  val defaultComicsDateQueryYears = 10L
  val defaultComicsOrderBy        = "title"

  private val privateKey: String = config.privateKey
  private val publicKey: String  = config.publicKey

  private val authParams = RequestHelpers.buildAuthParams(publicKey, privateKey)

  private implicit val client: Service[Request, Response] = Http.client
    .withTls("gateway.marvel.com")
    .withDecompression(enabled = true)
    .newService("gateway.marvel.com:443")

  private val baseUrl = "https://gateway.marvel.com/v1/public"

  override def fetchMarvelCharacter(name: String): IO[Either[MarvelApiError, List[MarvelApiCharacterResult]]] = {
    val url = s"$baseUrl/characters?name=$name&$authParams"
    PaginationHelpers.fetchAllPages[MarvelApiCharacterResult](url)
  }

  override def fetchMarvelCharacterComics(
    id: Int,
    startYear: Option[Int],
    orderBy: Option[String]
  ): IO[Either[MarvelApiError, List[MarvelApiComicsResult]]] = {

    val dateQuery    = RequestHelpers.resolveDateQueryParameter(startYear, defaultComicsDateQueryYears)
    val orderByQuery = s"orderBy=${orderBy.getOrElse(defaultComicsOrderBy)}"

    val url = s"$baseUrl/characters/$id/comics?$dateQuery&$orderByQuery&$authParams"

    PaginationHelpers.fetchAllPages[MarvelApiComicsResult](url)
  }
}
