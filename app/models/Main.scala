package models

import models.util.db.DBHandler

object Main {
  def main(args: Array[String]) {
    DBHandler.resetTables()

    val go = new GeneticOrganiser

    go.generateInitialOrganisms()

    val os = DBHandler.allOrganisms

    println(os.mkString("\n"))

    go.moveToNextGeneration()

  }
}
