package hmda.model.filing;

sealed trait FilingStatus {
  def code: Int
  def message: String
}

case object NotStarted extends FilingStatus {
  override def code: Int = 1
  override def message: String = "not-started"
}
case object InProgress extends FilingStatus {
  override def code: Int = 2
  override def message: String = "in-progress"
}
case object Completed extends FilingStatus {
  override def code: Int = 3
  override def message: String = "completed"
}
case object Cancelled extends FilingStatus {
  override def code: Int = -1
  override def message: String = "cancelled"
}

case class Filing(
    period: String = "",
    lei: String = "",
    status: FilingStatus = NotStarted,
    filingRequired: Boolean = false,
    start: Long = 0L,
    end: Long = 0l
) {
  def isEmpty: Boolean =
    period == "" && lei == "" && status == NotStarted && filingRequired == false && start == 0L && end == 0L
}
