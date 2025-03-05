package com.lucas.utils

import cats.effect.IO
import com.typesafe.config.ConfigFactory

case class AppConfig(privateKey: String, publicKey: String)

object AppConfig {
  private def config = IO(ConfigFactory.load())

  def buildAppConfig(): IO[AppConfig] =
    for {
      rawConfig <- config
      privateKey = rawConfig.getString("marvel.api.private-key")
      publicKey  = rawConfig.getString("marvel.api.public-key")
    } yield AppConfig(privateKey, publicKey)
}
