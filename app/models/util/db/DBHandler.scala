package models.util.db

import scalikejdbc._

object DBHandler {
  lazy implicit val session = {
    Class.forName("org.postgresql.Driver")
    ConnectionPool.singleton("jdbc:postgresql://localhost/spirovolution", null, null)

    GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(
      enabled = false
    )

    AutoSession
  }

  def resetTables() = {
    dropTables()
    initialiseTables()
  }

  def initialiseTables() = {
    Seq(
      sql"""
          CREATE TABLE organism (
            id INT PRIMARY KEY,
            fields JSON,
            yes_votes INT,
            no_votes INT,
            first_generation INT,
            last_generation INT
          )
      """,
      sql"""
          CREATE TABLE active (
            organism_id INT REFERENCES organism(id)
          )
      """
    ).map(_.update.apply())
  }

  def dropTables() = {
    Seq(
      sql"""
           DROP TABLE IF EXISTS active
         """,
      sql"""
           DROP TABLE IF EXISTS organism
         """
    ).map(_.update.apply())
  }
}
