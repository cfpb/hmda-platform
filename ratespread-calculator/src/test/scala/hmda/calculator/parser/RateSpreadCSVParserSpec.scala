package hmda.calculator.apor

import org.scalatest.{MustMatchers, WordSpec}
import hmda.calculator.parser.APORCsvParser

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RateSpreadCSVParserSpec extends WordSpec with MustMatchers {
    val lockInDate1 =
      LocalDate.parse("2017-01-02", DateTimeFormatter.ISO_LOCAL_DATE)
    val lockInDate2 =
      LocalDate.parse("2017-01-09", DateTimeFormatter.ISO_LOCAL_DATE)
val fileSource = scala.io.Source.fromURL(
    getClass.getResource("/yieldTableFixed.txt"))

  val apors = fileSource
    .getLines()
    .map(x => APORCsvParser(x))
    .toList

    "Apor Parser" must {
        "create APOR object from file" in {
            apors mustBe List(
              APOR(
                lockInDate1,
                List(3.52, 3.38, 3.47, 3.47, 3.5, 3.5, 3.75, 3.75, 3.9, 3.9, 3.9, 3.9, 3.62, 3.62, 3.62, 3.62, 3.62, 3.62, 3.62, 3.62, 3.62, 3.62, 4.36, 4.36, 4.36, 4.36, 4.36, 4.36, 4.36, 4.36, 4.36, 4.36, 4.36, 4.36, 4.36, 4.36, 4.36, 4.36, 4.36, 4.36, 4.36, 4.36, 4.36, 4.36, 4.36, 4.36, 4.36, 4.36, 4.36, 4.36)
              ),
              APOR(
                lockInDate2,
                List(3.52, 3.39, 3.41, 3.41, 3.49, 3.49, 3.77, 3.77, 3.93, 3.93, 3.93, 3.93, 3.51, 3.51, 3.51, 3.51, 3.51, 3.51, 3.51, 3.51, 3.51, 3.51, 4.24, 4.24, 4.24, 4.24, 4.24, 4.24, 4.24, 4.24, 4.24, 4.24, 4.24, 4.24, 4.24, 4.24, 4.24, 4.24, 4.24, 4.24, 4.24, 4.24, 4.24, 4.24, 4.24, 4.24, 4.24, 4.24, 4.24, 4.24)
              )
            )
        }     
    }
}