package models.util.db

import javassist.compiler.ast.Variable

import models.{FieldDefinition, Organism}
import org.json4s._
import org.json4s.jackson.JsonMethods
import org.json4s.jackson.JsonMethods._
import play.api.Logger
import scalikejdbc._

import scala.util.Random

object DBHandler {

  private val log = Logger("Main")

  lazy implicit val session = {
    Class.forName("org.postgresql.Driver")
    ConnectionPool.singleton("jdbc:postgresql://localhost/spirovolution", "misha", null)

    GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(
      enabled = false
    )

    AutoSession
  }

  /**
    * Reset all the tables in the database
    */
  def resetTables(): Unit = {
    log.info("Resetting tables")

    dropTables()
    initialiseTables()
  }

  /**
    * Initialise all tables
    */
  def initialiseTables(): Unit = {
    log.info("Initialising tables")

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
          CREATE TABLE pool (
            id SERIAL PRIMARY KEY,
            title TEXT,
            fields JSON
          )
         """,
      sql"""
          CREATE TABLE active (
            organism_id SERIAL REFERENCES organism(id),
            pool_id SERIAL REFERENCES pool(id)
          )
        """
    ).map(_.update.apply())
  }

  /**
    * Drop all tables
    */
  def dropTables(): Unit = {
    log.info("Dropping tables")

    Seq(
      sql"""
           DROP TABLE IF EXISTS active
         """,
      sql"""
           DROP TABLE IF EXISTS pool
         """,
      sql"""
           DROP TABLE IF EXISTS organism
         """
    ).map(_.update.apply())
  }

  /**
    * Get all active organisms
    *
    * @return
    */
  def activeOrganisms(pool: String): Seq[Organism] = {
    sql"""
         SELECT O.id, O.fields, O.score, O.vote_amount, O.first_generation, O.last_generation
         FROM active A, organism O, pool P
         WHERE A.organism_id = O.id
         AND A.pool_id = P.id
         AND P.title = $pool
       """
      .map(r => resultSetToOrganism(r))
      .list.apply()
  }

  /**
    * All organism to have ever existed
    *
    * @return
    */
  def allOrganisms: Seq[Organism] = {
    sql"""
         SELECT O.id, O.fields, O.score, O.vote_amount, O.first_generation, O.last_generation
         FROM organism O
       """
      .map(r => resultSetToOrganism(r))
      .list.apply()
  }

  def organismsOfGeneration(generation: Int, pool: String): Seq[Organism] = {
    sql"""
         SELECT O.id, O.fields, O.score, O.vote_amount, O.first_generation, O.last_generation
         FROM organism O, pool P, active A
         WHERE O.id = A.organism_id
         AND P.id = A.pool_id
         AND P.title = $pool
         AND O.first_generation <= $generation
         AND O.last_generation >= $generation
       """.map(resultSetToOrganism).list.apply()
  }

  /**
    * @return the next organism to be rated
    */
  def organismToRate(pool: String): Option[Organism] = {
    activeOrganisms(pool).sortBy(_.voteAmount) match {
      case Seq() =>
        None
      case os =>
        val head = os.head
        val sameVoteAmount = os.filter(_.voteAmount == head.voteAmount)
        Some(sameVoteAmount(Random.nextInt(sameVoteAmount.length)))
    }
  }

  /**
    * Insert an organism and make it active
    *
    * @param o the organism to insert
    * @return the id of the organism
    */
  def insertOrganismAsActive(o: Organism, pool: String): Long = {
    val id = insertOrganism(o)

    val poolId = getPoolId(pool).get

    sql"""
          INSERT INTO active (organism_id, pool_id)
          VALUES ($id, $poolId)
      """.update.apply()

    id
  }

  /**
    * Insert an organism without making it active
    *
    * @param o organism to insert
    * @return the id of the organism
    */
  def insertOrganism(o: Organism): Long = {
    log.info(s"Inserting an organism: $o")

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

  /**
    * Update the organism's generation fields
 *
    * @param o the organism
    */
  def organismMovedToGeneration(o: Organism): Unit = {
    log.info(s"Moving organism to new generation: $o")

    sql"""
         UPDATE organism
         SET vote_amount = ${o.voteAmount}, last_generation = ${o.lastGeneration}
         WHERE id = ${o.id.get}
       """.update.apply()
  }

  /**
    * Insert a pool
 *
    * @param pool title of the pool
    * @param fields the fields in the pool
    * @return id of the pool
    */
  def insertPool(pool: String, fields: String): Long = {
    getPoolId(pool) match {
      case Some(id) => id
      case None =>
        log.info(s"Making new pool: $pool")
        sql"""
              INSERT INTO pool (title, fields)
              VALUES ($pool, CAST($fields AS JSON))
           """.updateAndReturnGeneratedKey().apply()
    }
  }

  /**
    * @param pool title of the pool
    * @return id of the pool
    */
  def getPoolId(pool: String) = {
    sql"""
         SELECT id FROM pool WHERE title = $pool
       """.map(_.long("id")).single.apply()
  }

  /**
    * @param pool title of the pool
    * @return the field definitions of the pool
    */
  def getPoolFieldDefinitions(pool: String): Seq[FieldDefinition] = {
    sql"""
         SELECT fields
         FROM pool
         WHERE title = $pool
       """.map(_.string("fields")).single.apply() match {
      case Some(rawJson) =>
        val json: JArray = JsonMethods.parse(rawJson).asInstanceOf[JArray]
        for {
          JObject(field) <- json.arr
          JField("name", JString(name)) <- field
          JField("upper_bound", JDouble(upperBound)) <- field
          JField("lower_bound", JDouble(lowerBound)) <- field
        } yield {
          FieldDefinition(name, lowerBound, upperBound)
        }
      case None => Seq()
    }
  }

  /**
    * Register a rating of an organism
    *
    * @param id the ID of the organism
    * @param rating the new rating of the organism
    * @return whether or not the registration succeeded
    */
  def rateOrganism(id: Long, rating: Double): Boolean = {
    log.info(s"Rating organism #$id as $rating")

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

  /**
    * @param o the organism to make inactive
    */
  def removeOrganism(o: Organism, pool: String): Unit = {
    log.info(s"Removing organism $o from pool $pool")

    val poolId = getPoolId(pool).get

    sql"""
         DELETE FROM active
         WHERE organism_id = ${o.id.get}
         AND pool_id = $poolId
       """.update.apply()
  }

  /**
    * Cast a result set from postgres to an organism
    *
    * @param r the result set
    * @return the organism
    */
  private def resultSetToOrganism(r: WrappedResultSet): Organism = new Organism(
    r.longOpt("id"),
    jsonFieldsToMap(r.string("fields")),
    r.int("score"),
    r.int("vote_amount"),
    r.int("first_generation"),
    r.int("last_generation"))

  /**
    * Cast a JSON string to a map of variables
    *
    * @param rawJson the raw JSON string
    * @return map of variable name to value
    */
  private def jsonFieldsToMap(rawJson: String): Map[String, Double] = {
    parse(rawJson) match {
      case JObject(vars: List[(String, JsonAST.JValue)]) =>
        vars.map { case JField(k, JDouble(v)) =>
          k -> v
        }.toMap
      case _ => Map()
    }
  }

  /**
    * Cast a map of variables to a JSON string
    *
    * @param map map of variable name to value
    * @return the raw JSON string
    */
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
