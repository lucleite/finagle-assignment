package com.lucas.marvel.services.traits

import com.lucas.marvel.domain.MarvelCharacterResponse

import scala.concurrent.duration.Duration

trait CacheService {

  def get(key: String): Option[MarvelCharacterResponse]

  def set(key: String, value: MarvelCharacterResponse, ttl: Duration): Unit
}
