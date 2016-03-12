package models

import controllers.Application
import models.util.db.DBHandler

object Main {
  def main(args: Array[String]) {
    if (args.nonEmpty && args(0) == "reset-tables") {
      DBHandler.resetTables()
      Application.setupPool(Application.defaultPool)
    } else {
      println("Unrecognised command")
    }
  }
}
