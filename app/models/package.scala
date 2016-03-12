import scala.util.Random

package object models {
  case class FieldDefinition(name: String, lowerBound: Double, upperBound: Double) {
    val range = upperBound - lowerBound

    /**
      * @return a random in-range value
      */
    def random = {
      name -> (lowerBound + (range * Random.nextDouble()))
    }
  }
}
