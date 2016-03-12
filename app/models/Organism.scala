package models

class Organism( val id: Option[Long],
                val fields: Map[String, Double],
                val score: Int, val voteAmount: Int,
                val firstGeneration: Int,
                val lastGeneration: Int) {

  override def toString: String = {
    s"Organism($id, $fields, $score, $voteAmount, $firstGeneration, $lastGeneration)"
  }
}
