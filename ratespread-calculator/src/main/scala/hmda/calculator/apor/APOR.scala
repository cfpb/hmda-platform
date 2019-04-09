package hmda.calculator.apor

import java.time.LocalDate

case class APOR(rateDate: LocalDate = LocalDate.now(),
                values: Seq[Double] = Nil) {
  def toCSV: String = {
    val year = rateDate.getYear
    val month = rateDate.getMonthValue
    val day = rateDate.getDayOfMonth
    val dateStr = s"$month/$day/$year"
    val valuesStr = values.mkString("|")
    s"$dateStr|$valuesStr"
  }
}
