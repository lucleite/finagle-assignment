package com.lucas.utils

/**
 * This object provides implicit conversions between Scala `Future` and Twitter
 * `Future`, enabling interoperability, especially for working with Cats Effect,
 * which lacks built-in support for Twitter `Future`.
 *
 * Original source:
 * https://finagle.github.io/finch/cookbook.html#converting-between-scala-futures-and-twitter-futures
 */
object TwitterScalaFutureConverters {
  import com.twitter.util.{Return, Throw, Future => TFuture}

  import scala.concurrent.{Future => SFuture, Promise => SPromise}

  implicit class RichTFuture[A](f: TFuture[A]) {
    def asScala: SFuture[A] = {
      val p: SPromise[A] = SPromise()
      f.respond {
        case Return(value)    => p.success(value)
        case Throw(exception) => p.failure(exception)
      }

      p.future
    }
  }
}
