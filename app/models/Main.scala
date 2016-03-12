package models

import models.util.db.DBHandler

object Main {
  def main(args: Array[String]) {
    DBHandler.resetTables()

    val o = new Organism(
      Map("x" -> 1.0, "y" -> 2.0, "z" -> 3.0),
      1, 2, 3, 4
    )

    DBHandler.insertOrganismAsActive(o)

    val os = DBHandler.getAllOrganisms()

    println(os.mkString("\n"))
  }
}
