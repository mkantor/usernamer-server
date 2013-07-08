package test

import play.api.test._
import play.api.test.Helpers._
import play.api.mvc._

import org.specs2.mutable._

import test.support._

import controllers._

/**
 * Controller tests should just test each controller method without worrying 
 * about HTTP at all. They should be as specific to the controller as possible, 
 * i.e. not testing controller route generation, etc. View stuff should 
 * probably be tested in integration tests.
 */
class UserControllerSpec extends Specification {
  "User controller" should {

    "have an index" in new WithFakeUsernamerApplication {
      val result: Result = User.index(FakeRequest())

      status(result) must equalTo(OK)
    }

    "allow creating users" in new WithFakeUsernamerApplication {
      val result: Result = User.create(FakeRequest().withFormUrlEncodedBody(
        "username" -> "test"
      ))

      // FIXME: This will probably change to either a redirect or 201 Created.
      status(result) must equalTo(OK)
    }

    "reject invalid requests to create users" in new WithFakeUsernamerApplication {
      val result: Result = User.create(FakeRequest()) // No username included.

      status(result) must equalTo(BAD_REQUEST)
    }

    /* TODO: Add tests for:
        - duplicate vs not duplicate?
    */
  }
}

