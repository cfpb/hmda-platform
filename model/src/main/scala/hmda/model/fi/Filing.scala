package hmda.model.fi

sealed trait FilingStatus
case object NotStarted extends FilingStatus
case object InProgress extends FilingStatus
case object Completed extends FilingStatus
case object Cancelled extends FilingStatus

case class Filing(period: String = "", institutionId: String = "", status: FilingStatus = NotStarted)
