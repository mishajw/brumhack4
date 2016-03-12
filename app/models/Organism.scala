package models

class Organism( val fields: Map[String, Double],
                val score: Int, val voteAmount: Int,
                val firstGeneration: Int,
                val lastGeneration: Int) {

  override def toString: String = {
    s"Organism($fields, $score, $voteAmount, $firstGeneration, $lastGeneration)"
  }
}
