package hmda.model.publication.reports

case class Disposition(
    disposition: ActionTakenTypeEnum,
    count: Int,
    value: Int
) {
  def +(disp: Disposition): Disposition = {
    Disposition(disposition, count + disp.count, value + disp.value)
  }
}
