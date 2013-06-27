package models

/* TODO:
  see http://stephane.godbillon.com/2012/10/18/writing-a-simple-app-with-reactivemongo-and-play-framework-pt-1.html#configuring_sbt
  most of that seems out of date? why are they dealing with BSON crap directly instead of using the json magic?
*/
import play.api.libs.json._
import play.api.libs.functional.syntax._
import reactivemongo.bson._

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
  implicit def fromJsObject(jsObject: JsObject): User = {
    // TODO: Convert the json object to a User.
    new User(
      (jsObject \ "username").as[String],
      (jsObject \ "deviceId").asOpt[String],
      (jsObject \ "deviceType").asOpt[String]
    )
  }

  def all(): List[User] = Nil
  // implicit object ArticleBSONReader extends BSONReader[Article] {
  //   def fromBSON(document: BSONDocument) :Article = {
  //     val doc = document.toTraversable
  //     Article(
  //       doc.getAs[BSONObjectID]("_id"),
  //       doc.getAs[BSONString]("title").get.value,
  //       doc.getAs[BSONString]("content").get.value,
  //       doc.getAs[BSONString]("publisher").get.value,
  //       doc.getAs[BSONDateTime]("creationDate").map(dt => new DateTime(dt.value)),
  //       doc.getAs[BSONDateTime]("updateDate").map(dt => new DateTime(dt.value))
  //     )
  //   }
  // }
  // implicit object ArticleBSONWriter extends BSONWriter[Article] {
  //   def toBSON(article: Article) = {
  //     val bson = BSONDocument(
  //       "_id" -> article.id.getOrElse(BSONObjectID.generate),
  //       "title" -> BSONString(article.title),
  //       "content" -> BSONString(article.content),
  //       "publisher" -> BSONString(article.publisher))
  //     if(article.creationDate.isDefined)
  //       bson += "creationDate" -> BSONDateTime(article.creationDate.get.getMillis)
  //     if(article.updateDate.isDefined)
  //       bson += "updateDate" -> BSONDateTime(article.updateDate.get.getMillis)
  //     bson
  //   }
  // }
}