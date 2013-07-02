package models

import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import play.modules.reactivemongo._
import play.modules.reactivemongo.json.collection.JSONCollection

import reactivemongo.api._
import reactivemongo.api.indexes._

import play.api.Play.current

case class User(
  // id: Option[BSONObjectID],
  username: String,
  deviceId: Option[String] = None,
  deviceType: Option[String] = None
) {
  /**
   * Persist the User in the db.
   */
  // FIXME: Should not leak the storage mechanism here.
  def save: Future[reactivemongo.core.commands.LastError] = {
    // Index usernames and do not allow duplicates.
    User.collection.indexesManager.ensure(
      Index(List("username" -> IndexType.Ascending), unique = true)
    )

    User.collection.save(Json.toJson(this))
  }

  /**
   * Check if a User already exists in persistent storage.
   */
   def exists: Future[Boolean] = {
    // Only check username, since that should be a unique identifier (users 
    // with the same username but different device are considered to be the 
    // same user).
    User.findOne(Map(
      "username" -> Some(this.username)
    )).map(optionalUser => optionalUser.nonEmpty)
   }

  /**
   * Get a string representation.
   */
  override def toString: String = this.username
}


object User {
  // TODO: Maybe make this a lazy val or something?
  private def collection: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("users")

  /**
   * Create a JSON formatter for User models.
   */
  implicit val format: Format[User] = (
    //(__ \ "id").formatNullable[BSONObjectID] and
    (__ \ "username").format[String] and
    (__ \ "deviceId").formatNullable[String] and
    (__ \ "deviceType").formatNullable[String]
  )(User.apply _, unlift(User.unapply))

  /**
   * Get all saved Users.
   */
  def all: Future[List[User]] = {
    this.findAll()
  }

  /**
   * Find a (future) List of Users based on constraints.
   */
  // FIXME: It'd be nice to make constraints be Map[String, Any] or something 
  // else more general, but I think I need to make my own serializer:
  // http://stackoverflow.com/questions/14467689/scala-to-json-in-play-framework-2-1
  // TODO: Also look into using tuples like Json.obj() for prettier calling.
  // Actually I can maybe solve this and the above problem by accepting tuples 
  // and just forwarding them along to Json.obj() instead of Json.toJson().
  def findAll(constraints: Map[String, Option[String]] = Map()): Future[List[User]] = {
    val usersCursor: Cursor[JsObject] = collection.find(
      Json.toJson(constraints)
    ).cursor[JsObject]

    val futureJsList: Future[List[JsObject]] = usersCursor.toList

    // Convert Future[List[JsObject]] into Future[List[User]].
    futureJsList.map({ jsList =>
      jsList.map(_.as[User])
    })
  }

  /**
   * Find a (future) single User based on constraints.
   */
  def findOne(constraints: Map[String, Option[String]] = Map()): Future[Option[User]] = {
    val futureJsOption: Future[Option[JsObject]] = collection.find(
      Json.toJson(constraints)
    ).one[JsObject]

    // Convert Future[Option[JsObject]] into Future[Option[User]].
    futureJsOption.map({ jsOption =>
      jsOption.map(_.as[User])
    })
  }
}