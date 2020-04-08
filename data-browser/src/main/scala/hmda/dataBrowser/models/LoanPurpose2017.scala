package hmda.dataBrowser.models

import enumeratum._
import scala.collection.immutable

sealed abstract class LoanPurpose2017(override val entryName: String)
    extends EnumEntry

object LoanPurpose2017 extends Enum[LoanPurpose] {
  val values: immutable.IndexedSeq[LoanPurpose] = findValues

  case object HomePurchase extends LoanPurpose2017("1")
  case object HomeImprovement extends LoanPurpose2017("2")
  case object Refinancing extends LoanPurpose2017("3")

  def validateLoanPurpose2017(
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
