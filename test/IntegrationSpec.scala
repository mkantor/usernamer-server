package test

import play.api.test._
import play.api.test.Helpers._

import org.specs2.mutable._

import test.support._

class IntegrationSpec extends Specification {

  "Application" should {

    "work from within a browser" in {
      running(TestServer(3333, application = new FakeUsernamerApplication()), HTMLUNIT) { browser =>
        browser.goTo("http://localhost:3333/")
        browser.pageSource must not be empty
      }
    }

    "send 404 on a bad request" in new WithFakeUsernamerApplication {
      route(FakeRequest(GET, "/this-route-should-never-match-a-resource")) must beNone
    }

  }

}