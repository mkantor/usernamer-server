package models

import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import play.modules.reactivemongo._
import play.modules.reactivemongo.json.collection.JSONCollection

import reactivemongo.api._

import play.api.Play.current

class User(
  // id: Option[BSONObjectID],
  val username: String,
  val deviceId: Option[String] = None,
  val deviceType: Option[String] = None
) {
  /**
   * Persist the User in the db.
   */
  // FIXME: Should not leak the storage mechanism here.
  def save: Future[reactivemongo.core.commands.LastError] = {
    // TODO: Validate that username is not duplicate.
    User.userCollection.save(Json.obj(
      "username" -> this.username
    ))
  }

  /**
   * Get a string representation.
   */
  override def toString: String = this.username
}

object User {
  /**
   * User objects know how to create themselves from JsObjects
   */
  implicit def fromJsObject(jsObject: JsObject): User = {
    new User(
      (jsObject \ "username").as[String],
      (jsObject \ "deviceId").asOpt[String],
      (jsObject \ "deviceType").asOpt[String]
    )
  }

  // TODO: Move me? Maybe make this a lazy val or something?
  private def userCollection: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("users")

  /**
   * Get all saved Users.
   */
  def all: Future[List[User]] = {
    // TODO? Is there an nicer way to find all? Passing an empty JsObject is 
    // kind of whack.
    val usersCursor: Cursor[JsObject] = userCollection.find(Json.obj()).cursor[JsObject]

    val futureUsersList: Future[List[JsObject]] = usersCursor.toList

    // Convert List[JsObject] into List[User].
    // I wish Scala would automagically use the implicit conversion for this, 
    // but despite the fact that the compiler knows how to convert JsObject => 
    // User, it doesn't know how to convert List[JsObject] => List[User].
    futureUsersList.map { _.map { User.fromJsObject } }
  }
}