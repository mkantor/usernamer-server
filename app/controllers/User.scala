package controllers

import scala.concurrent._
import scala.concurrent.duration._

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.validation._
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits._

object User extends Controller {

  val userForm: Form[models.User] = Form(
    mapping(
      "username" -> nonEmptyText.verifying(uniqueUsername)
    )(
      (username) => models.User(username, None, None)
    )(
      (user: models.User) => Some(user.username)
    )
  )

  private def uniqueUsername = Constraint[String]("Username must be unique") { username =>
    val proposedUser: models.User = new models.User(username)
    // Sadly we need to block here since this can't return a Future.
    if(Await.result(proposedUser.exists, Duration.Inf)) {
      Invalid(ValidationError("Username is already taken"))
    } else {
      Valid
    }
  }

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
        hasErrors = { userFormWithErrors =>
          // Respond appropriately based on the nature of the error(s).

          // FIXME: This is a really lame way to determine the error condition. 
          // Ideally I'd have a separate type of FormError or somehow gain 
          // access to the Constraint to check its type.
          val usernameError = userFormWithErrors.error("username").get
          if(usernameError.isInstanceOf[FormError] && usernameError.message == "Username is already taken") {
            models.User.all.map { users =>
              Conflict(views.html.usernamer(users, userFormWithErrors))
            }
          } else {
            models.User.all.map { users =>
              BadRequest(views.html.usernamer(users, userFormWithErrors))
            }
          }
        },
        success = { newUser =>
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