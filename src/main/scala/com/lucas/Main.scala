package com.lucas

import cats.effect.{IO, Resource, ResourceApp}
import com.lucas.marvel.api.{HealthStatusEndpoint, MarvelCharactersEndpoint}
import com.lucas.marvel.api.swagger.{SwaggerDocsEndpoint, SwaggerJsonLoader, SwaggerUIEndpoint}
import com.lucas.marvel.client.impl.MarvelApiClientImpl
import com.lucas.marvel.services.impl.{CacheServiceImpl, MarvelCharactersServiceImpl}
import com.lucas.utils.{AppConfig, LoggingUtils}
import com.typesafe.scalalogging.LazyLogging
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._

object Main extends ResourceApp.Forever with LazyLogging {

  LoggingUtils.disableNettyDebugLogging()

  val apiVersion = "v1"

  override def run(args: List[String]): Resource[IO, Unit] = for {
    config                  <- Resource.eval(AppConfig.buildAppConfig())
    marvelClient            <- Resource.pure(new MarvelApiClientImpl(config))
    cacheService            <- Resource.pure(new CacheServiceImpl)
    marvelCharactersService <- Resource.pure(new MarvelCharactersServiceImpl(marvelClient, cacheService))
    marvelEndpoint           = new MarvelCharactersEndpoint(marvelCharactersService, apiVersion).endpoint
    swaggerJson             <- SwaggerJsonLoader.acquire
    swaggerDocsEndpoint      = new SwaggerDocsEndpoint(swaggerJson, apiVersion).endpoint
    swaggerUIEndpoint        = new SwaggerUIEndpoint(apiVersion).endpoints
    healthStatusEndpoint     = new HealthStatusEndpoint(apiVersion).endpoint
    _ <-
      Bootstrap[IO]
        .serve[Application.Json](marvelEndpoint :+: swaggerDocsEndpoint :+: swaggerUIEndpoint :+: healthStatusEndpoint)
        .listen(":8080")
  } yield ()
}
