package hmda.model.institution

import enumeratum.{ Enum, EnumEntry }

/**
 * Determines whether a given institution type is a depository or non-depository
 */
sealed trait DepositoryType extends EnumEntry

object DepositoryType extends Enum[DepositoryType] {

  val values = findValues

  case object Depository extends DepositoryType
  case object NonDepository extends DepositoryType
  case object UndeterminedDepositoryType extends DepositoryType
}

/**
 * The type of financial institution
 */
sealed abstract class InstitutionType(override val entryName: String, val depositoryType: DepositoryType) extends EnumEntry with Serializable

object InstitutionType extends Enum[InstitutionType] {

  import DepositoryType._

  val values = findValues

  case object Bank extends InstitutionType("bank", Depository)
  case object CreditUnion extends InstitutionType("credit-union", Depository)
  case object SavingsAndLoan extends InstitutionType("savings-and-loan", Depository)

  case object IndependentMortgageCompany extends InstitutionType("independent-mortgage-company", NonDepository)

  // an FI with either of these two types will generally have a parent, but the code does not enforce that constraint.
  case object MBS extends InstitutionType("mortgage-banking-subsidiary", NonDepository)
  case object Affiliate extends InstitutionType("affiliate", NonDepository)

  case object UndeterminedInstitutionType extends InstitutionType("undetermined-institution-type", UndeterminedDepositoryType)

}
