package models

import org.json4s.JsonAST.JObject
import org.json4s._

class Organism( val id: Option[Long],
                val fields: Map[String, Double],
                val score: Int, val voteAmount: Int,
                val firstGeneration: Int,
                val lastGeneration: Int) {

  def toJson: JObject = {
    val json: JObject = JObject(List(
      "id" -> JInt(id.get),
      "score" -> JInt(score),
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

  override def toString: String = {
    s"Organism($id, $fields, $score, $voteAmount, $firstGeneration, $lastGeneration)"
  }
}
