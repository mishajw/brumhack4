package controllers

import models.{FieldDefinition, GeneticOrganiser}
import models.util.db.DBHandler
import org.json4s._
import org.json4s.JsonAST.{JObject, JString}
import org.json4s.jackson.JsonMethods
import org.json4s.jackson.JsonMethods._
import play.api.mvc._

object Application extends Controller {

  private val defaultPool = "main"

  private val defaultFieldDefinitions = Seq(
    FieldDefinition("rotatable", 0, 1),
    FieldDefinition("offset", 0, 1),
    FieldDefinition("colour", 0, 1)
  )

  /**
    * Home page
    */
  def index = Action {
    Ok(views.html.index())
  }

  def setupPool(pool: String) = Action { implicit request =>
    val rawJson = try {
      request.body.asFormUrlEncoded.get("fields").head
    } catch {
      case e: Exception =>
        fieldDefinitionsToJson(defaultFieldDefinitions)
    }

    // Create the pool
    DBHandler.insertPool(pool, rawJson)
    // Create the first generation of the pool
    GeneticOrganiser.generateInitialOrganisms(pool)

    Ok("Done")
  }

  /**
    * Get the next organism to rate
    */
  def getNextWithPool(pool: String) = Action { implicit request =>
    DBHandler.organismToRate(pool) match {
      case None => errorJson("No organisms left.")
      case Some(o) => Ok(stringifyJson(o.toJson))
    }
  }

  def getNext = getNextWithPool(defaultPool)

  /**
    * Rate an organism by ID
    */
  def rateOrganism = Action { implicit request =>

    if (request.body.asFormUrlEncoded.isEmpty)
      errorJson("Couldn't get POST variables")

    val postVars = request.body.asFormUrlEncoded.get.flatMap { case (k, vs) =>
      vs.headOption match {
        case Some(v) => Some(k -> v)
        case None => None
      }
    }

    if (!(postVars contains "id") || !(postVars contains "rating")) {
      errorJson("Not enough parameters: need ID and rating")
    }

    val pool = {
      if (postVars contains "pool") postVars("pool")
      else defaultPool
    }

    try {
      val id = postVars("id").toLong
      val rating = postVars("rating").toDouble

      DBHandler.rateOrganism(id, rating) match {
        case true =>
          GeneticOrganiser.checkForNewGeneration(pool)
          Ok("Done")
        case false => errorJson("Couldn't rate organism, doesn't exist")
      }
    } catch {
      case e: Throwable =>
        errorJson("Input was not correct type")
    }
  }

  private def fieldDefinitionsToJson(fields: Seq[FieldDefinition]): String = {
    val json = JArray(fields.map { f => JObject(List(
      "name" -> JString(f.name),
      "upper_bound" -> JDouble(f.upperBound),
      "lower_bound" -> JDouble(f.lowerBound)
    ))}.toList)

    JsonMethods.compact(JsonMethods.render(json))
  }

  /**
    * JSON object to string
    */
  private def stringifyJson(json: JValue) =
    JsonMethods.pretty(JsonMethods.render(json))

  /**
    * Serve an error in JSON format
 *
    * @param errorMsg the error to display
    */
  private def errorJson(errorMsg: String) = {
    BadRequest(stringifyJson(JObject(List(
      "error" -> JString(errorMsg))
    )))
  }
}