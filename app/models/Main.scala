package models

import models.util.db.DBHandler

object Main {
  def main(args: Array[String]) {
    DBHandler.resetTables()

    val pool = "secondary"

    val fieldDefinitions = Seq(
      FieldDefinition("rot", 0, 1),
      FieldDefinition("offset", 0, 1),
      FieldDefinition("colour", 0, 1)
    )

    DBHandler.insertPool(pool, fieldDefinitions)

    GeneticOrganiser.generateInitialOrganisms(pool)
    println(DBHandler.allOrganisms.mkString("\n"))

    GeneticOrganiser.moveToNextGeneration(pool)
    println(DBHandler.allOrganisms.mkString("\n"))
  }
}
