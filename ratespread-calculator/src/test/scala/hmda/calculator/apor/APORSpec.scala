package hmda.calculator.apor

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import org.scalatest.FunSuite

class APORSpec extends FunSuite{
  val apor = APOR(
    LocalDate.parse("2020-04-01", DateTimeFormatter.ISO_LOCAL_DATE),
    List()
  )
  test("Testing APOR.toSCV"){
    apor.toCSV
  }
}
