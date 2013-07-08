package controllers

import scala.concurrent._
import scala.concurrent.duration._

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits._

object User extends Controller {

  val userForm = Form(
    "username" -> nonEmptyText.verifying("Username is already taken", { username =>
      // Sadly we need to block here since .verifying() needs a Boolean and not 
      // a Future.
      Await.result(new models.User(username).exists, Duration.Inf) == false
    })
  )

  /**
   * Allow user to enter a username.
   */
  def index = Action { implicit request =>
    Async {
      models.User.all.map({ users =>
        Ok(views.html.usernamer(users, userForm))
      })
    }
  }

  /**
   * Attempt to add a new username.
   */
  def create = Action { implicit request =>
    Async {
      userForm.bindFromRequest.fold(
        userFormWithErrors => models.User.all.map { users =>
          BadRequest(views.html.usernamer(users, userFormWithErrors))
        },
        username => {
          val newUser: models.User = new models.User(username)
          newUser.save.map(lastError => Async {
            // TODO: Handle errors.
            // TODO: If I make a resource for individual users (/users/:username) 
            // then make this issue a 201 Created response with a Location 
            // header pointing to the new user resource. See 
            // http://tools.ietf.org/html/draft-ietf-httpbis-p2-semantics-21#section-5.3.3
            models.User.all.map({ users =>
              Ok(views.html.usernamer(users, userForm, "Added new user '%s'.".format(newUser)))
            })
          })
        }
      )
    }
  }
}