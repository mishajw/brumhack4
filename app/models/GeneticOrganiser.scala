package models

import scala.util.Random

class GeneticOrganiser() {
  var generation: Int = 0

  case class Variable(name: String, lowerBound: Double, upperBound: Double) {
    def random = {
      name -> (lowerBound + ((upperBound - lowerBound) * Random.nextDouble()))
    }
  }

  val variables = Seq(
    Variable("rot", 0, 1),
    Variable("offset", 0, 1),
    Variable("colour", 0, 1)
  )

  def randomOrganism: Organism = {
    new Organism(variables.map(_.random).toMap, generation)
  }
}
