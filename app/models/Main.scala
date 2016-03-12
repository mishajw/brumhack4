package models

import models.util.db.DBHandler

object Main {
  def main(args: Array[String]) {
    DBHandler.resetTables()
  }
}
