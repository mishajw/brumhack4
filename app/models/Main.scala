package models

import models.util.db.DBHandler

object Main {
  def main(args: Array[String]) {
    if (args.nonEmpty && args(0) == "reset-tables") {
      DBHandler.resetTables()
    } else {

    }
  }
}
