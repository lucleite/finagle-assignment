package com.lucas.utils

import ch.qos.logback.classic.{Level, LoggerContext}
import org.slf4j.LoggerFactory

object LoggingUtils {

  /**
   * Couldn't suppress the io.netty logging from the logback.xml
   */
  def disableNettyDebugLogging(): Unit =
    LoggerFactory.getILoggerFactory
      .asInstanceOf[LoggerContext]
      .getLogger("io.netty")
      .setLevel(Level.WARN)
}
