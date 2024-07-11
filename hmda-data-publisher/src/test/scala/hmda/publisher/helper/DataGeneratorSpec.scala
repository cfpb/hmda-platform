package hmda.publisher.helper

import org.scalatest.{Matchers, WordSpec}
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class DateGeneratorSpec extends WordSpec with Matchers {

  val generator = DateGenerator
  val currentDate: LocalDate = LocalDate.now().minusDays(1)
  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  val actualDateString: String = currentDate.format(formatter) + "-"
  val actualQuarterlyDateString: String = currentDate.format(formatter) + "_"

  "date generator" should {

    "return correct current date" in {
      assert(generator.currentDate == actualDateString)
    }

    "return correct current quarterly date" in {
      assert(generator.currentQuarterlyDate == actualQuarterlyDateString)
    }
  }

}