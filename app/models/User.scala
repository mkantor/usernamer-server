package models

import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import play.modules.reactivemongo._
import play.modules.reactivemongo.json.collection.JSONCollection

import reactivemongo.api._

import play.api.Play.current

case class User(
  // id: Option[BSONObjectID],
  username: String,
  deviceId: Option[String] = None,
  deviceType: Option[String] = None
)

// TODO? Use Reads? Maybe like this?
// implicit object UserReads extends Reads[User] {
//   def read(jsValue: JsValue) =
//     User(
//       (jsObject \ "username").as[String],
//       (jsObject \ "deviceId").asOpt[String],
//       (jsObject \ "deviceType").asOpt[String]
//     )
// }

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

  // TODO: Move me? Maybe make this a lazy val or something instead?
  private def userCollection: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("users")

  /**
   * Get all saved Users.
   */
  def all: Future[List[User]] = {

    // let's do our query
    // TODO? Is there an nicer way to find all?
    val usersCursor: Cursor[JsObject] = userCollection.find(Json.obj()).cursor[JsObject]

    val futureUsersList: Future[List[JsObject]] = usersCursor.toList

    // Convert List elements into actual Users.
    // I wish this were able to use the implicit conversion for this, but 
    // despite teh fact that the compiler knows how to convert JsObject => User, it 
    // doesn't know how to convert List[JsObject] => List[User]
    futureUsersList.map { _.map { User.fromJsObject } }
  }
}