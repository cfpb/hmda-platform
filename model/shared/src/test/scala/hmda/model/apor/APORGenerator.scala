package hmda.model.apor

import java.time.LocalDate

import org.scalacheck.Gen

object APORGenerator {

  def APORGen: Gen[APOR] = {
    for {
      date <- localDateGen
      aporList <- aporListGen
    } yield APOR(date, aporList)
  }

  private def localDateGen: Gen[LocalDate] = {
    val minDate = LocalDate.of(2000, 1, 1).toEpochDay
    val currentYear = LocalDate.now().getYear
    val maxDate = LocalDate.of(currentYear, 1, 1).toEpochDay
    Gen.choose(minDate, maxDate).map(i => LocalDate.ofEpochDay(i))
  }

  private def aporListGen: Gen[Seq[Double]] = {
    Gen.listOfN(50, Gen.choose(0, Double.MaxValue))
  }

}
