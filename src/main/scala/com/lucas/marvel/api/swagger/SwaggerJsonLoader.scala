package com.lucas.marvel.api.swagger

import cats.effect.{IO, Resource}

import scala.io.{Codec, Source}

object SwaggerJsonLoader {

  val fileName = "swagger.json"

  def acquire: Resource[IO, String] =
    Resource
      .make(IO(Source.fromResource(fileName)((Codec.UTF8))))(src => IO(src.close()))
      .evalMap(read(_))

  private def read(src: Source): IO[String] = IO(src.getLines().mkString)
}
