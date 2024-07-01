package hmda.publisher.helper

import org.scalatest.{Matchers, WordSpec}

class DateGeneratorSpec extends WordSpec with Matchers {
  val generator = DateGenerator
  "date generator" should {

    "return correct current date" in {
      assert(generator.currentDate == "2024-06-25-")
    }

    "return correct current quarterly date" in {
      assert(generator.currentQuarterlyDate == "2024-06-25_")
    }
  }

}