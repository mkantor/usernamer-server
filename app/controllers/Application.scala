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
  def usernamer = Action {
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
    usernamerForm.bindFromRequest.fold(
      errors => Async {
        User.all.map { users =>
          BadRequest(views.html.usernamer(users, errors))
        }
      },
      username => {
        // TODO: new User(username).
        new Status(204) // "No Content"
      }
    )
  }
}