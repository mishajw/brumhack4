package models

import models.util.db.DBHandler

import scala.util.Random

class GeneticOrganiser() {

  val generationSize = 100
  val parentPercentage = 0.5
  val parentProgressRate = 0.05
  lazy val breedSize = (generationSize * (1 - parentProgressRate)).toInt
  val mutationProbability = 0.1
  val mutationAmount = 0.2

  var generation: Int = 0

  case class Variable(name: String, lowerBound: Double, upperBound: Double) {
    val range = upperBound - lowerBound

    def random = {
      name -> (lowerBound + (range * Random.nextDouble()))
    }
  }

  val variables = Seq(
    Variable("rot", 0, 1),
    Variable("offset", 0, 1),
    Variable("colour", 0, 1)
  )

  def checkForNewGeneration(): Unit = {
    if (!everythingRated) return

    moveToNextGeneration()
  }

  def moveToNextGeneration(): Unit = {
    generation += 1

    val lastGeneration = DBHandler.activeOrganisms
    val parents = bestParents(lastGeneration)
    val children = mutate(breed(parents))
    val progressedParents = Random.shuffle(parents).take(generationSize - breedSize)

    lastGeneration
      .filterNot(progressedParents.contains)
      .foreach(DBHandler.removeOrganism)

    children
      .foreach(DBHandler.insertOrganismAsActive)
  }

  def generateInitialOrganisms() = {
    for (i <- 0 to generationSize) {
      DBHandler.insertOrganismAsActive(randomOrganism)
    }
  }

  private def bestParents(os: Seq[Organism]): Seq[Organism] = {
    val all = os.sortBy(_.rating)
    all.take((all.length * parentPercentage).toInt)
  }

  private def breed(parents: Seq[Organism]): Seq[Organism] = {
    for (i <- 0 to breedSize) yield {
      new Organism(
        parents(Random.nextInt(parents.size)),
        parents(Random.nextInt(parents.size)),
        generation)
    }
  }

  private def mutate(os: Seq[Organism]): Seq[Organism] = os.map { o =>
    Random.nextDouble() < mutationProbability match {
      case true => new Organism(o.id,
        {
          o.fields.map { case (k, v) =>
            val range: Double = (Random.nextDouble() * 2) - 1
            k -> (v + (range * mutationAmount * getVariable(k).range))
          }
        },
        o.rating, o.voteAmount, o.firstGeneration, o.lastGeneration
      )
      case false => o
    }
  }

  private def getVariable(name: String) = {
    variables.filter(_.name == name).head
  }

  private def everythingRated =
    !DBHandler.activeOrganisms.exists(_.rating == 0)

  def randomOrganism: Organism = {
    new Organism(variables.map(_.random).toMap, generation)
  }
}
