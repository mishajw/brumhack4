package models

import models.util.db.DBHandler

object Main {
  def main(args: Array[String]) {
    DBHandler.resetTables()

    GeneticOrganiser.generateInitialOrganisms()

    val os = DBHandler.allOrganisms

    println(os.mkString("\n"))

    GeneticOrganiser.moveToNextGeneration()

  }
}
