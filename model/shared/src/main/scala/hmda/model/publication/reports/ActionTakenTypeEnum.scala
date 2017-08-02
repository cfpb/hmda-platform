package hmda.model.publication.reports

import enumeratum.values.{ IntEnum, IntEnumEntry }

sealed abstract class ActionTakenTypeEnum(
  override val value: Int,
  val description: String
) extends IntEnumEntry

object ActionTakenTypeEnum extends IntEnum[ActionTakenTypeEnum] {

  val values = findValues

  case object ApplicationReceived extends ActionTakenTypeEnum(0, "Application Received")
  case object LoansOriginated extends ActionTakenTypeEnum(1, "Loans Originated")
  case object ApprovedButNotAccepted extends ActionTakenTypeEnum(2, "Apps. Approved But Not Accepted")
  case object ApplicationsDenied extends ActionTakenTypeEnum(3, "Applications Denied")
  case object ApplicationsWithdrawn extends ActionTakenTypeEnum(4, "Applications Withdrawn")
  case object ClosedForIncompleteness extends ActionTakenTypeEnum(5, "Files Closed for Incompleteness")
  case object LoanPurchased extends ActionTakenTypeEnum(6, "Loan Purchased by your Institution")
  case object PreapprovalDenied extends ActionTakenTypeEnum(7, "Preapproval Request Denied by Financial Institution")
  case object PreapprovalApprovedButNotAccepted extends ActionTakenTypeEnum(8, "Preapproval Request Approved but not Accepted by Financial Institution")
}
