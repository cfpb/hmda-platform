package hmda.dataBrowser.models

import enumeratum._
import scala.collection.immutable

sealed abstract class LoanType(override val entryName: String) extends EnumEntry

object LoanType extends Enum[LoanType] {
  val values: immutable.IndexedSeq[LoanType] = findValues

  case object Conventional extends LoanType("1")
  case object FHA extends LoanType("2")
  case object VA extends LoanType("3")
  case object USDA extends LoanType("4")

  def validateLoanType(
      rawLoanType: Seq[String]): Either[Seq[String], Seq[LoanType]] = {
    val potentialLoanTypes = rawLoanType.map(loanType =>
      (loanType, LoanType.withNameInsensitiveOption(loanType)))

    val isValidLoanType = potentialLoanTypes.map(_._2).forall(_.isDefined)

    if (isValidLoanType) Right(potentialLoanTypes.flatMap(_._2))
    else
      Left(potentialLoanTypes.collect {
        case (input, None) => input
      })
  }
}
