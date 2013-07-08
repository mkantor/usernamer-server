package test

import play.api.test._
import play.api.test.Helpers._
import play.api.mvc._

import org.specs2.mutable._

import test.support._

import controllers._

class ApplicationControllerSpec extends Specification {

  "Application controller" should {
    "redirect to the User controller" in {
      val result: Result = Application.index(FakeRequest())

      status(result) must equalTo(SEE_OTHER)
      header("Location", result).get must equalTo(routes.User.index.url)
    }
  }

}