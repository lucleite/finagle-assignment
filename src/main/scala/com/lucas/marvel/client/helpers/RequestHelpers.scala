package com.lucas.marvel.client.helpers

import cats.effect.IO
import com.lucas.marvel.client.{MarvelApiError, MarvelApiHttpError, UnknownError}
import com.lucas.utils.DigestUtils
import com.lucas.utils.TwitterScalaFutureConverters.RichTFuture
import com.twitter.finagle.{http, Service}
import com.twitter.finagle.http.{Request, Response}
import com.typesafe.scalalogging.LazyLogging

import java.time.{Instant, LocalDate}

object RequestHelpers extends LazyLogging {

  private[client] def sendRequest(
    url: String
  )(implicit client: Service[Request, Response]): IO[Either[MarvelApiError, Response]] = {
    val request = Request(url)
    request.method = http.Method.Get
    request.headerMap.add("Accept-Encoding", "gzip")

    IO.fromFuture(IO {
      client(request).asScala
    }).map { response =>
      if (response.statusCode == 200) Right(response)
      else Left(MarvelApiHttpError(response.statusCode, response.contentString))
    }.handleErrorWith { t: Throwable =>
      logger.error(s"Error sending request: $t")
      IO.pure(Left(UnknownError(t))) }
  }

  private[client] def buildAuthParams(publicKey: String, privateKey: String): String = {
    val ts   = Instant.now().getEpochSecond.toString
    val hash = DigestUtils.md5Hash(ts + privateKey + publicKey)
    s"ts=$ts&apikey=$publicKey&hash=$hash"
  }

  private[client] def resolveDateQueryParameter(startYear: Option[Int], defaultComicsDateQueryYears: Long): String = {
    // Only used if startYear is not provided
    def getDefaultDateQuery = {
      val today     = LocalDate.now
      val startDate = today.minusYears(defaultComicsDateQueryYears)
      val endDate   = today
      s"dateRange=$startDate,$endDate"
    }

    startYear.fold(getDefaultDateQuery)(year => s"startYear=$year")
  }

}
