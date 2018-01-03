package hmda.model.apor

import java.time.LocalDate

import org.scalacheck.Gen

object APORGenerator {

  implicit def APORGen: Gen[APOR] = {
    for {
      date <- localDateGen
      aporList <- aporListGen
    } yield APOR(date, aporList)
  }

  implicit def rateTypeGen: Gen[RateType] = {
    Gen.oneOf(FixedRate, VariableRate)
  }

  implicit def localDateGen: Gen[LocalDate] = {
    val minDate = LocalDate.of(2000, 1, 3).toEpochDay
    val currentYear = LocalDate.now().getYear
    val maxDate = LocalDate.of(currentYear, 1, 1).toEpochDay
    Gen.choose(minDate, maxDate).map(i => LocalDate.ofEpochDay(i))
  }

  implicit def aporListGen: Gen[Seq[Double]] = {
    Gen.listOfN(50, Gen.choose(0, 12.0))
  }

}
