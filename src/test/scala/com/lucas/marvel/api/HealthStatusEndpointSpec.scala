package com.lucas.marvel.api

import cats.effect.testing.scalatest.AsyncIOSpec
import com.twitter.finagle.http.Status
import io.finch.Input
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.OptionValues

class HealthStatusEndpointSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with OptionValues {

  "HealthStatusEndpoint" - {
    "should return OK status with service message when service is up" in {
      val endpoint = new HealthStatusEndpoint("v1").endpoint
      val input    = Input.get("/v1/health")

      endpoint(input).output.value asserting { output =>
        output.status shouldBe Status.Ok
        output.value shouldBe "Service is up and running"
      }
    }
  }
}
