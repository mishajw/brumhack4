package models.util.db

import models.Organism
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
            id SERIAL PRIMARY KEY,
            fields JSON,
            score INT,
            vote_amount INT,
            first_generation INT,
            last_generation INT
          )
      """,
      sql"""
          CREATE TABLE active (
            organism_id SERIAL REFERENCES organism(id)
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

  def getAllOrganisms(): Seq[Organism] = {
    sql"""
         SELECT fields, score, vote_amount, first_generation, last_generation
         FROM active A, organism O
         WHERE A.organism_id = O.id
       """.map(r => new Organism(
      jsonFieldsToMap(r.string("fields")),
      r.int("score"),
      r.int("vote_amount"),
      r.int("first_generation"),
      r.int("last_generation")
    )).list.apply()
  }

  def insertOrganismAsActive(o: Organism): Long = {
    val id = insertOrganism(o)

    sql"""
          INSERT INTO active (organism_id)
          VALUES ($id)
      """.update.apply()

    id
  }

  def insertOrganism(o: Organism): Long = {
    sql"""
         INSERT INTO organism (fields, score, vote_amount, first_generation, last_generation)
         VALUES (
            CAST(${mapToJsonFields(o.fields)} AS JSON),
            ${o.score},
            ${o.voteAmount},
            ${o.firstGeneration},
            ${o.lastGeneration}
         )
       """.updateAndReturnGeneratedKey().apply()
  }

  private def jsonFieldsToMap(rawJson: String): Map[String, Any] = {
    Map()
  }

  private def mapToJsonFields(map: Map[String, Any]): String = {
    "{}"
  }
}
