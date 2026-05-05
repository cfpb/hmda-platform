package hmda.dataBrowser.models

import enumeratum._
import scala.collection.immutable

sealed abstract class LoanPurpose(override val entryName: String)
    extends EnumEntry

object LoanPurpose extends Enum[LoanPurpose] {
  val values: immutable.IndexedSeq[LoanPurpose] = findValues

  case object HomePurchase extends LoanPurpose("1")
  case object HomeImprovement extends LoanPurpose("2")
  case object Refinancing extends LoanPurpose("31")
  case object CashOutRefinancing extends LoanPurpose("32")
  case object OtherPurpose extends LoanPurpose("4")
  case object NotApplicable extends LoanPurpose("5")

  def validateLoanPurpose(
      rawLoanPurpose: Seq[String]): Either[Seq[String], Seq[LoanPurpose]] = {
    val potentialLoanPurposes = rawLoanPurpose.map(loanPurpose =>
      (loanPurpose, LoanPurpose.withNameInsensitiveOption(loanPurpose)))

    val isValidLoanPurpose = potentialLoanPurposes.map(_._2).forall(_.isDefined)

    if (isValidLoanPurpose) Right(potentialLoanPurposes.flatMap(_._2))
    else
      Left(potentialLoanPurposes.collect {
        case (input, None) => input
      })
  }
}
