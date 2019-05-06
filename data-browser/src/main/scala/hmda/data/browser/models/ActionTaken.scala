package hmda.data.browser.models

 import enumeratum.values._
import scala.collection.immutable

 sealed abstract class ActionTaken(override val value: Int) extends IntEnumEntry

 object ActionTaken extends IntEnum[ActionTaken] {
  val values: immutable.IndexedSeq[ActionTaken] = findValues

  case object LoansOriginated extends ActionTaken(1)
  case object ApplicationsApprovedButNotAccepted extends ActionTaken(2)
  case object ApplicationsDeniedByFinancialInstitution extends ActionTaken(3)
  case object ApplicationsWithdrawnByApplicant extends ActionTaken(4)
  case object FileClosedForIncompleteness extends ActionTaken(5)
  case object PurchasedLoans extends ActionTaken(6)
  case object Placeholder1 extends ActionTaken(7)
  case object Placeholder2 extends ActionTaken(8)
}