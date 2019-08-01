package hmda.dataBrowser.models

import enumeratum._
import scala.collection.immutable

sealed abstract class LoanProduct(override val entryName: String)
    extends EnumEntry

object LoanProduct extends Enum[LoanProduct] {
  val values: immutable.IndexedSeq[LoanProduct] = findValues

  case object ConventionalFirst extends LoanProduct("Conventional:First Lien")
  case object FHAFirst extends LoanProduct("FHA:First Lien")
  case object VAFirst extends LoanProduct("VA:First Lien")
  case object FSAFirst extends LoanProduct("FSA/RHS:First Lien")
  case object ConventionalSubordinate
      extends LoanProduct("Conventional:Subordinate Lien")
  case object FHASubordinate extends LoanProduct("FHA:Subordinate Lien")
  case object VASubordinate extends LoanProduct("VA:Subordinate Lien")
  case object FSASubordinate extends LoanProduct("FSA/RHS:Subordinate Lien")

  def validateLoanProducts(
      rawLoanProducts: Seq[String]): Either[Seq[String], Seq[LoanProduct]] = {
    val potentialLoanProducts = rawLoanProducts.map(loanProduct =>
      (loanProduct, LoanProduct.withNameInsensitiveOption(loanProduct)))

    val isValidLoanProducts =
      potentialLoanProducts.map(_._2).forall(_.isDefined)

    if (isValidLoanProducts) Right(potentialLoanProducts.flatMap(_._2))
    else
      Left(potentialLoanProducts.collect {
        case (input, None) => input
      })
  }
}
