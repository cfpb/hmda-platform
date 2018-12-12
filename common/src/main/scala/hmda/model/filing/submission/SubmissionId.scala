package hmda.model.filing.submission

object SubmissionId {
  def apply(s: String): SubmissionId = {
    val components = s.split('-')
    val lei = components.head
    val period = components.tail.head
    val seqNr = components.reverse.head.toInt
    SubmissionId(lei, period, seqNr)
  }
}

case class SubmissionId(lei: String = "",
                        period: String = "",
                        sequenceNumber: Int = 0) {
  override def toString: String = s"$lei-$period-$sequenceNumber"

  def isEmpty: Boolean = {
    lei == "" && period == "" && sequenceNumber == 0
  }
}
