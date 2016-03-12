package models.util.db

import models.Organism
import org.json4s._
import org.json4s.jackson.JsonMethods._
import scalikejdbc._

import scala.util.Random

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

  def activeOrganisms: Seq[Organism] = {
    sql"""
         SELECT id, fields, score, vote_amount, first_generation, last_generation
         FROM active A, organism O
         WHERE A.organism_id = O.id
       """
      .map(r => resultSetToOrganism(r))
      .list.apply()
  }

  def allOrganisms: Seq[Organism] = {
    sql"""
         SELECT id, fields, score, vote_amount, first_generation, last_generation
         FROM organism
       """
      .map(r => resultSetToOrganism(r))
      .list.apply()
  }

  def organismToRate: Option[Organism] = {
    activeOrganisms.sortBy(_.voteAmount) match {
      case Seq() =>
        None
      case os =>
        val head = os.head
        val sameVoteAmount = os.filter(_.voteAmount == head.voteAmount)
        Some(sameVoteAmount(Random.nextInt(sameVoteAmount.length)))
    }
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
            ${o.rating},
            ${o.voteAmount},
            ${o.firstGeneration},
            ${o.lastGeneration}
         )
       """.updateAndReturnGeneratedKey().apply()
  }

  def rateOrganism(id: Long, rating: Double): Boolean = {
    val orgOpt: Option[Organism] = sql"""
         SELECT * FROM organism
         WHERE id = $id
       """.map(resultSetToOrganism).single.apply()

    orgOpt match {
      case None => false
      case Some(o) =>
        o.rate(rating)

        sql"""
             UPDATE organism
             SET score = ${o.rating}, vote_amount = ${o.voteAmount}
             WHERE id = $id
           """.update.apply()

        true
    }
  }

  def removeOrganism(o: Organism): Unit = {
    sql"""
         DELETE FROM active
         WHERE organism_id = ${o.id.get}
       """.update.apply()
  }

  private def resultSetToOrganism(r: WrappedResultSet): Organism = new Organism(
    r.longOpt("id"),
    jsonFieldsToMap(r.string("fields")),
    r.int("score"),
    r.int("vote_amount"),
    r.int("first_generation"),
    r.int("last_generation"))

  private def jsonFieldsToMap(rawJson: String): Map[String, Double] = {
    parse(rawJson) match {
      case JObject(vars: List[(String, JsonAST.JValue)]) =>
        vars.map { case JField(k, JDouble(v)) =>
          k -> v
        }.toMap
      case _ => Map()
    }
  }

  private def mapToJsonFields(map: Map[String, Double]): String = {
    val json: JObject =
      JObject(
        map.map({ case (k, v) =>
          k -> JDouble(v.toDouble)
        }).toList
      )

    compact(render(json))
  }
}
