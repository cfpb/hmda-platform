package hmda.model.publication.reports

case class Disposition(
  disposition: ActionTakenTypeEnum,
  count: Int,
  value: Int
)
