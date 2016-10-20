package hmda.api.model

case class Receipt(timestamp: Long, receipt: String)
case object Receipt {
  def empty: Receipt = Receipt(0L, "")
}
