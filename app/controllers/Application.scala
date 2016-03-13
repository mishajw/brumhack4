package controllers

import models.{Organism, FieldDefinition, GeneticOrganiser}
import models.util.db.DBHandler
import org.json4s._
import org.json4s.JsonAST.{JObject, JString}
import org.json4s.jackson.JsonMethods
import org.json4s.jackson.JsonMethods._
import play.api.Logger
import play.api.mvc._

object Application extends Controller {

  private val log = Logger("Main")

  val defaultPool = "main"

  private val defaultFieldDefinitions = Seq(
    FieldDefinition("shpSides", 1, 7),
    FieldDefinition("shpNum", 1, 500),
    FieldDefinition("shpRad", 0, 1),
    FieldDefinition("shpRot", 0, 100),
    FieldDefinition("shpOffset", 0, 4),
    FieldDefinition("shpSize", 0, 0.5),
    FieldDefinition("shpColR", 0, 1),
    FieldDefinition("shpColG", 0, 1),
    FieldDefinition("shpColB", 0, 1),
    FieldDefinition("shpColA", 0.1, 0.5),
    FieldDefinition("angleRev", 1, 10),
    FieldDefinition("rotRev", 0, 8)

  )

  /**
    * Home page
    */
  def indexWithPool(pool: String) = Action {
    Ok(views.html.index())
  }

  def index = indexWithPool(defaultPool)

  def setupPoolAction(pool: String) = Action { implicit request =>
    try {
      val rawJson = request.body.asFormUrlEncoded.get("fields").head
      setupPool(rawJson)
    } catch {
      case e: Exception =>
        setupPool(pool)
    }

    Ok("Done")
  }

  def setupPool(pool: String, rawJson: String = fieldDefinitionsToJson(defaultFieldDefinitions)): Unit = {
    // Create the pool
    DBHandler.insertPool(pool, rawJson)
    // Create the first generation of the pool
    GeneticOrganiser.generateInitialOrganisms(pool)
  }

  def getAverageOfGenerationWithPool(pool: String) = Action { implicit request =>
    val generation = request.getQueryString("generation").get.toInt

    val ofGeneration = DBHandler.organismsOfGeneration(generation, pool)

    if (ofGeneration.isEmpty) {
      errorJson("No organisms of generation")
    } else {
      Ok(stringifyJson(new Organism(ofGeneration, generation).toJson))
    }
  }

  def getAverageOfGeneration = getAverageOfGenerationWithPool(defaultPool)

  /**
    * Get the next organism to rate
    */
  def getNextWithPool(pool: String) = Action { implicit request =>
    if (DBHandler.getPoolId(pool).isEmpty) {
      setupPool(pool, fieldDefinitionsToJson(defaultFieldDefinitions))
    }

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
