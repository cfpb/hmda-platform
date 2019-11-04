package hmda.model.filing.submission

object SubmissionId {
  def apply(s: String): SubmissionId =
    s.split('-').toList match {
      case lei :: year :: quarter :: seqNr :: Nil =>
        SubmissionId(lei, s"$year-$quarter", seqNr.toInt)

      case lei :: year :: seqNr :: Nil =>
        SubmissionId(lei, year, seqNr.toInt)

      case _ =>
        throw new IllegalArgumentException(s"Unable to parse $s into a valid Submission ID")
    }
}

case class SubmissionId(lei: String = "", period: String = "", sequenceNumber: Int = 0) {
  override def toString: String = s"$lei-$period-$sequenceNumber"

  def isEmpty: Boolean =
    lei == "" && period == "" && sequenceNumber == 0
}
