package models

import models.util.db.DBHandler

object Main {
  def main(args: Array[String]) {
    DBHandler.resetTables()

    DBHandler.insertOrganismAsActive(new Organism(
      Map("x" -> 1.0, "y" -> 2.0, "z" -> 3.0), 1
    ))

    DBHandler.insertOrganismAsActive(new Organism(
      Map("x" -> 1.0, "y" -> 2.0, "z" -> 3.0), 1
    ))

    DBHandler.insertOrganismAsActive(new Organism(
      Map("x" -> 1.0, "y" -> 2.0, "z" -> 3.0), 1
    ))

    val os = DBHandler.allOrganisms

    println(os.mkString("\n"))
  }
}
