package hmda.model.publication.reports

case class Disposition(
    disposition: DispositionEnum,
    count: Int,
    value: Int
) {
  def +(disp: Disposition): Disposition = {
    Disposition(disposition, count + disp.count, value + disp.value)
  }
}
