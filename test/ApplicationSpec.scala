package test

import play.api.test._
import play.api.test.Helpers._

import org.specs2.mutable._

import test.support._

class ApplicationSpec extends Specification {

  "Application" should {

    "send 404 on a bad request" in new WithFakeUsernamerApplication {
      route(FakeRequest(GET, "/this-route-should-never-match-a-resource")) must beNone
    }

    "render the index page" in new WithFakeUsernamerApplication {
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      charset(home) must beSome("utf-8")
      contentAsString(home) must not be empty
    }
  }
}