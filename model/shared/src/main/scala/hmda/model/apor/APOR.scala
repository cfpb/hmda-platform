package hmda.model.apor

import java.time.LocalDate

case class APOR(loanTerm: LocalDate = LocalDate.now(), values: Seq[Double] = Nil) {
  def toCSV: String = {
    val year = loanTerm.getYear
    val month = loanTerm.getMonthValue
    val day = loanTerm.getDayOfMonth
    val dateStr = s"$month/$day/$year"
    val valuesStr = values.mkString("|")
    s"$dateStr|$valuesStr"
  }
}
