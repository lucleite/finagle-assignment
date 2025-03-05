package com.lucas.marvel.services.impl

import com.lucas.marvel.domain.MarvelCharacterResponse
import com.lucas.marvel.services.traits.CacheService
import com.typesafe.scalalogging.LazyLogging

import scala.collection.concurrent.TrieMap
import scala.concurrent.duration.Duration

/**
 * A simple, yet thread-safe, service for caching data with a time-to-live (TTL)
 * mechanism.
 *
 * This implementation uses a concurrent `TrieMap` to store key-value pairs with
 * an associated expiry time.
 *
 * Once the TTL expires, the cached value will no longer be accessible.
 *
 * https://scala-lang.org/api/2.13.16/scala/collection/concurrent/TrieMap.html
 *
 * Another options previously considered:
 *   - Caffeine
 *   - Cats.Ref
 *   - Java.util.concurrent.ConcurrentHashMap
 */
class CacheServiceImpl extends CacheService with LazyLogging {
  private val cache = new TrieMap[String, CacheEntry]()

  override def get(key: String): Option[MarvelCharacterResponse] =
    cache
      .get(key)
      .filter(_.expirationTime > System.currentTimeMillis()) match {
      case Some(entry) =>
        logger.debug(s"Cache hit for key: $key")
        Some(entry.value)
      case None =>
        logger.debug(s"Cache miss for key: $key")
        None
    }

  override def set(key: String, value: MarvelCharacterResponse, ttl: Duration): Unit = {
    cache
      .put(key, CacheEntry(value, System.currentTimeMillis() + ttl.toMillis))
    logger.debug(s"Cache entry set for key: $key")
  }
}

case class CacheEntry(value: MarvelCharacterResponse, expirationTime: Long)
