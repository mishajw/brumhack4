package controllers

import models.util.db.DBHandler
import org.json4s.JValue
import org.json4s.JsonAST.{JString, JObject}
import org.json4s.jackson.JsonMethods
import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def getNext = Action {
    DBHandler.organismToRate match {
      case None => errorJson("No organisms left.")
      case Some(o) => Ok(stringifyJson(o.toJson))
    }
  }

  private def stringifyJson(json: JValue) =
    JsonMethods.pretty(JsonMethods.render(json))

  private def errorJson(errorMsg: String) = {
    BadRequest(stringifyJson(JObject(List(
      "error" -> JString(errorMsg))
    )))
  }
}