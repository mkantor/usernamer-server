package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.data._
import play.api.data.Forms._

import play.modules.reactivemongo._
import play.modules.reactivemongo.json.collection.JSONCollection

import reactivemongo.api._

import scala.concurrent.Future

import models.User

// import play.api.Play.current

object Application extends Controller with MongoController {

  /*
   * Get a JSONCollection (a Collection implementation that is designed to work
   * with JsObject, Reads and Writes.)
   * Note that the `collection` is not a `val`, but a `def`. We do _not_ store
   * the collection reference to avoid potential problems in development with
   * Play hot-reloading.
   */
  // TODO: move this into User?
  private def users: JSONCollection = db.collection[JSONCollection]("users")

  val usernamerForm = Form(
    "username" -> nonEmptyText
  )

  /**
   * Allows user to enter a username.
   */
  def usernamer = Action {
    Async {
      // let's do our query
      // TODO: probably move most of this into User
      //  User.findAll?
      val cursor: Cursor[JsObject] = users.
        // find all people with name `name`
        find(Json.obj()). // FIXME: is there an easier way to find all? // find(Json.obj("name" -> name)).
        // sort them by creation date
        // sort(Json.obj("created" -> -1)).
        // perform the query and get a cursor of JsObject
        cursor[JsObject]

      // gather all the JsObjects in a list
      val futureUsersList: Future[List[JsObject]] = cursor.toList

      // transform the list into a JsArray
      // val futureUsersJsonArray: Future[JsArray] = futureUsersList.map { users =>
      //   Json.arr(users)
      // }

      // everything's ok! Let's reply with the list (turned into a list of Users)
      futureUsersList.map { users =>


        // I wish this were able to actually use the implicit conversion, but 
        // even though the compiler knows how to convert JsObject => User, it 
        // doesn't know how to convert List[JsObject] => List[User]
        val userModels: List[User] = users.map(User.fromJsObject)

        Logger.debug(userModels.toString) // XXX
        Ok(views.html.usernamer(userModels, usernamerForm))



      }
      // TODO? maybe can be fancy with something like this instead?
      //  futureUsersList.map Ok(views.html.usernamer(_))
      // or even
      //  Ok(views.html.usernamer(futureUsersList))
    }


    // Async {
    //   // the future cursor of documents
    //   val allUsers = users.find(Json.obj()) // FIXME: is there an easier way to find all? TODO: try val allUsers = users.find(Nil)
    //   // build (asynchronously) a list containing all the articles

    //   // FIXME: allUsers.toList is not a thing
    //   // allUsers.toList.map { users =>
    //   //   Ok(views.html.usernamer(users))
    //   // }

    // }
    // XXX
    // Logger.debug("in usernamer action")

    // Ok(views.html.usernamer())
  }

  /**
   * Attempts to add a new username.
   */
  def nameuser = Action { implicit request =>
    usernamerForm.bindFromRequest.fold(
      errors => BadRequest(views.html.usernamer(User.all(), errors)/* TODO: should have a body for this, maybe use index with errors on the form (see the form helper for magic) */),
      username => {
        // TODO: new User(username)
        new Status(204) // "No Content"
      }
    )
  
    // // XXX
    // Logger.debug("in nameuser action")


    // // TODO: Try to add the user, fail if duplicate.

    // Async {
    //   val json = Json.obj(
    //     "username" -> "username goes here"
    //   )

    //   users.insert(json).map(lastError =>
    //     Ok("Mongo LastError: %s".format(lastError)) // FIXME? What type does this have to be? WTF?
    //   )
    // }

    // Respond with a successful status code and no HTTP body.
    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.5
    //new Status(204) // "No Content"
  }
}