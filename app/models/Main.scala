package models

import models.util.db.DBHandler

object Main {
  def main(args: Array[String]) {
    DBHandler.resetTables()

    val go = new GeneticOrganiser

    for (i <- 0 to 10) {
      DBHandler.insertOrganismAsActive(go.randomOrganism)
    }

    val os = DBHandler.allOrganisms

    println(os.mkString("\n"))
  }
}
