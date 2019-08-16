package hmda.dataBrowser.models

import enumeratum._
import scala.collection.immutable

sealed abstract class ActionTaken(override val entryName: String)
    extends EnumEntry

object ActionTaken extends Enum[ActionTaken] {
  val values: immutable.IndexedSeq[ActionTaken] = findValues

  case object LoansOriginated extends ActionTaken("1")
  case object ApplicationsApprovedButNotAccepted extends ActionTaken("2")
  case object ApplicationsDeniedByFinancialInstitution extends ActionTaken("3")
  case object ApplicationsWithdrawnByApplicant extends ActionTaken("4")
  case object FileClosedForIncompleteness extends ActionTaken("5")
  case object PurchasedLoans extends ActionTaken("6")
  case object Placeholder1 extends ActionTaken("7")
  case object Placeholder2 extends ActionTaken("8")

  def validateActionsTaken(
      rawActionsTaken: Seq[String]): Either[Seq[String], Seq[ActionTaken]] = {
    val potentialActions =
      rawActionsTaken.map(action =>
        (action, ActionTaken.withNameInsensitiveOption(action)))
    val isActionsValid = potentialActions.map(_._2).forall(_.isDefined)

    if (isActionsValid)
      Right(potentialActions.flatMap(_._2))
    else
      Left(
        potentialActions.collect {
          case (input, None) => input
        }
      )
  }
}
