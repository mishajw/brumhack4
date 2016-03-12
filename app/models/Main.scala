package models

import models.util.db.DBHandler

object Main {
  def main(args: Array[String]) {
//    DBHandler.resetTables()

    val pool = "secondary"

    GeneticOrganiser.generateInitialOrganisms(pool)
    println(DBHandler.allOrganisms.mkString("\n"))

    GeneticOrganiser.moveToNextGeneration(pool)
    println(DBHandler.allOrganisms.mkString("\n"))
  }
}
