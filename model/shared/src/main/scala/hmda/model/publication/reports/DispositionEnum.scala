package hmda.model.publication.reports

import enumeratum.values.{ StringEnum, StringEnumEntry }

sealed abstract class DispositionEnum(
  override val value: String,
  val numberValue: Int
) extends StringEnumEntry

object DispositionEnum extends StringEnum[DispositionEnum] {

  val values = findValues

  case object ApplicationReceived extends DispositionEnum("Application Received", 0)
  case object LoansOriginated extends DispositionEnum("Loans Originated", 1)
  case object ApprovedButNotAccepted extends DispositionEnum("Apps. Approved But Not Accepted", 2)
  case object ApplicationsDenied extends DispositionEnum("Applications Denied", 3)
  case object ApplicationsWithdrawn extends DispositionEnum("Applications Withdrawn", 4)
  case object ClosedForIncompleteness extends DispositionEnum("Files Closed for Incompleteness", 5)
  case object LoanPurchased extends DispositionEnum("Loan Purchased by your Institution", 6)
  case object PreapprovalDenied extends DispositionEnum("Preapproval Request Denied by Financial Institution", 7)
  case object PreapprovalApprovedButNotAccepted extends DispositionEnum("Preapproval Request Approved but not Accepted by Financial Institution", 8)

  case object FannieMae extends DispositionEnum("Fannie Mae", 1)
  case object GinnieMae extends DispositionEnum("Ginnie Mae", 2)
  case object FreddieMac extends DispositionEnum("FreddieMac", 3)
  case object FarmerMac extends DispositionEnum("FarmerMac", 4)
  case object PrivateSecuritization extends DispositionEnum("Private Securitization", 5)
  case object CommercialBank extends DispositionEnum("Commercial bank, savings bank or association", 6)
  case object FinanceCompany extends DispositionEnum("Life insurance co., credit union, finance co.", 7)
  case object Affiliate extends DispositionEnum("Affiliate institution", 8)
  case object OtherPurchaser extends DispositionEnum("Other", 9)
}
