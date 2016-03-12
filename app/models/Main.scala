package models

import models.util.db.DBHandler

object Main {
  def main(args: Array[String]) {
    DBHandler.resetTables()

    val o = new Organism(
      Map("x" -> 1, "y" -> 2, "z" -> 3),
      1, 2, 3, 4
    )

    DBHandler.insertOrganismAsActive(o)

    val os = DBHandler.getAllOrganisms()

    println(os.mkString("\n"))
  }
}
