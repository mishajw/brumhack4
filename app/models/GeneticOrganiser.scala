package models

import models.util.db.DBHandler

import scala.util.Random

object GeneticOrganiser {

  /**
    * Generation variables
    */
  val generationSize = 10
  val parentPercentage = 0.5
  val parentProgressRate = 0.1
  lazy val breedSize = (generationSize * (1 - parentProgressRate)).toInt
  val mutationProbability = 0.1
  val mutationAmount = 0.2

  /**
    * What generation it's on
    */
  var generation: Int = 0

  /**
    * A variable that can be changed
    * @param name the name of the variable
    * @param lowerBound max value
    * @param upperBound min value
    */
  case class Variable(name: String, lowerBound: Double, upperBound: Double) {
    val range = upperBound - lowerBound

    /**
      * @return a random in-range value
      */
    def random = {
      name -> (lowerBound + (range * Random.nextDouble()))
    }
  }

  /**
    * The variables for this session
    */
  val variables = Seq(
    Variable("rot", 0, 1),
    Variable("offset", 0, 1),
    Variable("colour", 0, 1)
  )

  /**
    * Check if there should be a new generation
    */
  def checkForNewGeneration(): Unit = {
    if (!everythingRated) return

    moveToNextGeneration()
  }

  /**
    * Move to the next generation
    */
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

  /**
    * Generate the first generation of organisms
    */
  def generateInitialOrganisms() = {
    for (i <- 0 to generationSize) {
      DBHandler.insertOrganismAsActive(randomOrganism)
    }
  }

  /**
    * @param os the organisms to choose from
    * @return the optimum parents from this set
    */
  private def bestParents(os: Seq[Organism]): Seq[Organism] = {
    val all = os.sortBy(_.rating)
    all.take((all.length * parentPercentage).toInt)
  }

  /**
    * Breed a set of parents to create children
    * @param parents the parents to breed
    * @return the lovely ity-bity babies
    */
  private def breed(parents: Seq[Organism]): Seq[Organism] = {
    for (i <- 0 to breedSize) yield {
      new Organism(
        parents(Random.nextInt(parents.size)),
        parents(Random.nextInt(parents.size)),
        generation)
    }
  }

  /**
    * Mutate children (not in a cruel way?)
    * @param os the organism to mutate
    * @return the mutated organisms
    */
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

  /**
    * Get a variable by a name
    * @param name the name of the variable
    * @return the variable
    */
  private def getVariable(name: String) = {
    variables.filter(_.name == name).head
  }

  /**
    * Check if everything has been rated
    * @return true if everything is rated
    */
  private def everythingRated =
    !DBHandler.activeOrganisms.exists(_.rating == 0)

  /**
    * Generate a random organism
    * @return the new organism
    */
  def randomOrganism: Organism = {
    new Organism(variables.map(_.random).toMap, generation)
  }
}
