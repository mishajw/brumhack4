package controllers

import models.GeneticOrganiser
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

  def rateOrganism = Action { implicit request =>
    Seq("id", "rating").map(request.getQueryString) match {
      case Seq(Some(idStr), Some(ratingStr)) =>
        try {
          val id = idStr.toLong
          val rating = ratingStr.toDouble

          DBHandler.rateOrganism(id, rating) match {
            case true =>
              GeneticOrganiser.checkForNewGeneration()
              Ok("Done")
            case false => errorJson("Couldn't rate organism, doesn't exist")
          }
        } catch {
          case e: Throwable =>
            errorJson("Input was not correct type")
        }
      case _ =>
        errorJson("Not enough parameters: need ID and rating")
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