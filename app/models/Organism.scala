package models

import org.json4s.JsonAST.JObject
import org.json4s._

class Organism(val id: Option[Long],
               val fields: Map[String, Double],
               var rating: Double,
               var voteAmount: Int,
               val firstGeneration: Int,
               var lastGeneration: Int) {

  /**
    * Initialise with basic fields
    * @param fields the fields of the shape
    * @param generation the generation it's in
    * @return new organism
    */
  def this(fields: Map[String, Double], generation: Int) =
    this(None, fields, 0, 0, generation, generation)

  /**
    * Initialise with two parents, and take the averages of their fields
    * @param mummy
    * @param daddy
    * @param generation
    */
  def this(mummy: Organism, daddy: Organism, generation: Int) = {
    this({
      mummy.fields.map { case (k, v) =>
        k -> (v + daddy.fields(k)) / 2
      }
    }, generation)
  }

  /**
    * Cast to JSON
    * @return JSON object
    */
  def toJson: JObject = {
    val json: JObject = JObject(List(
      "id" -> JInt(id.get),
      "score" -> JDouble(rating),
      "vote_amount" -> JInt(voteAmount),
      "first_generation" -> JInt(firstGeneration),
      "last_generation" -> JInt(lastGeneration),
      "fields" -> JObject(
          fields.map({ kv =>
            (kv._1.toString, JDouble(kv._2.toDouble))
          }).toList
        )
    ))

    json
  }

  /**
    * @param newRating the new rating to register
    */
  def rate(newRating: Double) = {
    rating = ((rating * voteAmount) + newRating) / (voteAmount + 1)
    voteAmount += 1
  }

  override def toString: String = {
    s"Organism($id, $fields, $rating, $voteAmount, $firstGeneration, $lastGeneration)"
  }
}
