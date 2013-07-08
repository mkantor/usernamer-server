package test

import scala.util.Random

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits._

import org.specs2.mutable._
import reactivemongo.core.errors.DatabaseException

import models.User
import test.support._

class UserModelSpec extends Specification {

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
      await(savedUser.save)
      val saved: Boolean = await(savedUser.exists)

      saved must equalTo(true)
    }


    "not exist if not saved" in new WithFakeUsernamerApplication {
      val id: String = getId
      val unsavedUser: User = new User(
        s"test-username-$id",
        Some(s"test-device-id-$id"),
        Some(s"test-device-type-$id")
      )
      val saved: Boolean = await(unsavedUser.exists)

      saved must equalTo(false)
    }


    "be findable if saved" in new WithFakeUsernamerApplication {
      val id: String = getId
      val savedUser: User = new User(
        s"test-username-$id",
        Some(s"test-device-id-$id"),
        Some(s"test-device-type-$id")
      )
      await(savedUser.save)

      val foundUser1: User = await(User.findOne(Map(
        "username" -> Some(savedUser.username)
      ))).get

      val foundUser2: User = await(User.findOne(Map(
        "username" -> Some(savedUser.username),
        "deviceId" -> savedUser.deviceId,
        "deviceType" -> savedUser.deviceType
      ))).get

      savedUser must equalTo(foundUser1)
      savedUser must equalTo(foundUser2)
    }


    "be findable along with other users" in new WithFakeUsernamerApplication {
      val id1: String = getId
      val id2: String = getId
      val findableDeviceId = "findable"
      val testUser1: User = new User(
        s"test-username-$id1",
        Some(findableDeviceId)
      )
      val testUser2: User = new User(
        s"test-username-$id2", 
        Some(findableDeviceId)
      )
      await(testUser1.save)
      await(testUser2.save)

      await(User.findAll(Map(
        "deviceId" -> Some(findableDeviceId)
      ))) must equalTo(List(testUser1, testUser2))
    }


    "disallow duplicate usernames" in new WithFakeUsernamerApplication {
      val sameUsername: String = s"test-username-${getId}"
      val testUser1: User = new User(sameUsername, Some("one"))
      val testUser2: User = new User(sameUsername, Some("two"))

      await(testUser1.save)

      await(testUser2.save) must throwA[DatabaseException]
    }
  }
}