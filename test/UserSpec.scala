package test

import scala.concurrent._
import scala.concurrent.duration._
import scala.util.Random

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits._

import org.specs2.mutable._

import models.User
import test.support._

class UserSpec extends Specification {

  /**
   * This will usually return non-ASCII characters.
   */
  private def getId: String = (new Random()).nextString(8)


  "A User" should {

    "extract into JSON" in {
      val id1: String = getId
      val id2: String = getId
      val testUser1: User = new User(
        s"test-username-$id1",
        Some(s"test-device-id-$id1"),
        Some(s"test-device-type-$id1")
      )
      val testUser2: User = new User(s"test-username-$id2")

      Json.toJson(testUser1) must equalTo(Json.obj(
        "username" -> testUser1.username,
        "deviceId" -> testUser1.deviceId,
        "deviceType" -> testUser1.deviceType
      ))
      Json.toJson(testUser2) must equalTo(Json.obj(
        "username" -> testUser2.username
      ))
    }


    "build itself from JSON" in {
      val id1: String = getId
      val id2: String = getId
      val id3: String = getId
      val json1: JsValue = Json.parse(s"""{
        "username": "test-username-$id1",
        "deviceId": "test-device-id-$id1",
        "deviceType": "test-device-type-$id1"
      }""")
      val json2: JsValue = Json.parse(s"""{
        "username": "test-username-$id2"
      }""")
      val json3: JsValue = Json.parse(s"""{
        "username": "test-username-$id3",
        "deviceId": null,
        "deviceType": null
      }""")

      json1.as[User] must equalTo(new User(
        s"test-username-$id1",
        Some(s"test-device-id-$id1"),
        Some(s"test-device-type-$id1")
      ))
      json2.as[User] must equalTo(new User(s"test-username-$id2"))
      json3.as[User] must equalTo(new User(s"test-username-$id3"))
    }


    "exist if saved" in new WithFakeUsernamerApplication {
      val id: String = getId
      val savedUser: User = new User(
        s"test-username-$id",
        Some(s"test-device-id-$id"),
        Some(s"test-device-type-$id")
      )
      Await.result(savedUser.save, Duration.Inf)
      val saved: Boolean = Await.result(savedUser.exists, Duration.Inf)

      saved must equalTo(true)
    }


    "not exist if not saved" in new WithFakeUsernamerApplication {
      val id: String = getId
      val unsavedUser: User = new User(
        s"test-username-$id",
        Some(s"test-device-id-$id"),
        Some(s"test-device-type-$id")
      )
      val saved: Boolean = Await.result(unsavedUser.exists, Duration.Inf)

      saved must equalTo(false)
    }


    "be findable if saved" in new WithFakeUsernamerApplication {
      val id: String = getId
      val savedUser: User = new User(
        s"test-username-$id",
        Some(s"test-device-id-$id"),
        Some(s"test-device-type-$id")
      )
      Await.result(savedUser.save, Duration.Inf)

      val foundUser1: User = Await.result(User.findOne(Map(
        "username" -> Some(savedUser.username)
      )), Duration.Inf).get

      val foundUser2: User = Await.result(User.findOne(Map(
        "username" -> Some(savedUser.username),
        "deviceId" -> savedUser.deviceId,
        "deviceType" -> savedUser.deviceType
      )), Duration.Inf).get

      savedUser must equalTo(foundUser1)
      savedUser must equalTo(foundUser2)
    }
  }
}