package models

import models.util.db.DBHandler
import org.json4s._
import org.json4s.jackson.JsonMethods._

object Main {
  def main(args: Array[String]) {
    DBHandler.resetTables()

    val pool = "main"

    val fieldDefinitions = Seq(
      FieldDefinition("rotatable", 0, 1),
      FieldDefinition("offset", 0, 1),
      FieldDefinition("colour", 0, 1)
    )

    DBHandler.insertPool(pool, fieldDefinitionsToJson(fieldDefinitions))

    GeneticOrganiser.generateInitialOrganisms(pool)
    println(DBHandler.allOrganisms.mkString("\n"))

    GeneticOrganiser.moveToNextGeneration(pool)
    println(DBHandler.allOrganisms.mkString("\n"))
  }

  private def fieldDefinitionsToJson(fields: Seq[FieldDefinition]): String = {
    val json = JArray(fields.map { f => JObject(List(
      "name" -> JString(f.name),
      "upper_bound" -> JDouble(f.upperBound),
      "lower_bound" -> JDouble(f.lowerBound)
    ))}.toList)

    compact(render(json))
  }
}
