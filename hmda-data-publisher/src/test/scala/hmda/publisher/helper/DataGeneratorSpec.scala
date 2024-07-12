package hmda.publisher.helper

import org.scalatest.{Matchers, WordSpec}
import scala.util.matching.Regex


class DateGeneratorSpec extends WordSpec with Matchers {

  val annualPattern: Regex = raw"\d{4}-\d{2}-\d{2}-".r
  val quarterPattern: Regex = raw"\d{4}-\d{2}-\d{2}_".r
  val generator = DateGenerator


  "date generator" should {

    "return a correctly formatted annual date string" in {
      val annualMatch = annualPattern.findFirstIn(generator.currentDate)
      assert(annualMatch.contains(generator.currentDate))
    }

    "return a correctly formatted quarterly date string" in {
      val quarterMatch = quarterPattern.findFirstIn(generator.currentQuarterlyDate)
      assert(quarterMatch.contains(generator.currentQuarterlyDate))
    }
  }

}