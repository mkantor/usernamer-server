package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits._

import models.User

object Application extends Controller {

  val usernamerForm = Form(
    "username" -> nonEmptyText
  )

  /**
   * Allows user to enter a username.
   */
  def usernamer = Action { implicit request =>
    Async {
      User.all.map { users =>
        Ok(views.html.usernamer(users, usernamerForm))
      }
    }
  }

  /**
   * Attempts to add a new username.
   */
  def nameuser = Action { implicit request =>
    Async {
      usernamerForm.bindFromRequest.fold(
        usernamerFormWithErrors => User.all.map { users =>
          BadRequest(views.html.usernamer(users, usernamerFormWithErrors))
        },
        username => {
          val newUser: User = new User(username)
          newUser.save.map(lastError => {
            // TODO: Handle errors.
            Redirect(routes.Application.usernamer).flashing(
              "success" -> "Added new user '%s'.".format(newUser)
            )
          })
        }
      )
    }
  }
}