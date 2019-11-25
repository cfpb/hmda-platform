package hmda.model.filing.submission
import hmda.utils.YearUtils.Period

object SubmissionId {
  def apply(s: String): SubmissionId =
    s.split('-').toList match {
      case lei :: year :: quarter :: seqNr :: Nil =>
        SubmissionId(lei, Period(year.toInt, Some(quarter)), seqNr.toInt)

      case lei :: year :: seqNr :: Nil =>
        SubmissionId(lei, Period(year.toInt, None), seqNr.toInt)

      case _ =>
        throw new IllegalArgumentException(s"Unable to parse $s into a valid Submission ID")
    }
}

case class SubmissionId(lei: String = "", period: Period = Period(2018, None), sequenceNumber: Int = 0) {
  override def toString: String = s"$lei-$period-$sequenceNumber"

  def isEmpty: Boolean =
    lei == "" && period == Period(2018, None) && sequenceNumber == 0
}
