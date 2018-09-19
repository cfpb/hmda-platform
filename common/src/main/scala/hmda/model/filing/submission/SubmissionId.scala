package hmda.model.filing.submission

case class SubmissionId(lei: String = "",
                        period: String = "",
                        sequenceNumber: Int = 0) {
  override def toString: String = s"$lei-$period-$sequenceNumber"

  def isEmpty: Boolean = {
    lei == "" && period == "" && sequenceNumber == 0
  }
}
