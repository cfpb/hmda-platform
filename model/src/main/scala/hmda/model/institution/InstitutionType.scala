package hmda.model.institution

import enumeratum.{ Enum, EnumEntry }

/**
 * Determines whether a given institution type is a depository or non-depository
 */
sealed trait DepositoryType extends EnumEntry

// FIXME: This could also be modeled as `isDepository: Option[Boolean]`.  Which is cleaner?
object DepositoryType extends Enum[DepositoryType] {

  val values = findValues

  case object Depository extends DepositoryType
  case object NonDepository extends DepositoryType
}

/**
 * The type of financial institution
 */
sealed abstract class InstitutionType(override val entryName: String, val depositoryType: Option[DepositoryType]) extends EnumEntry

object InstitutionType extends Enum[InstitutionType] {

  import DepositoryType._

  val values = findValues

  case object Bank extends InstitutionType("bank", Some(Depository))
  case object CreditUnion extends InstitutionType("credit-union", Some(Depository))
  case object SavingsAndLoan extends InstitutionType("savings-and-loan", Some(Depository))

  // may or may not have a parent (of which to be independent). is this name better, or IndependentMBS? or other?
  case object IndependentMortgageCompany extends InstitutionType("independent-mortgage-company", Some(NonDepository))

  // must an FI with either of these two types always have a parent?
  // also: please advise on names; there may be more appropriate industry terms to use.
  case object DependentMortgageCompany extends InstitutionType("dependent-mortgage-company", Some(NonDepository))
  case object Affiliate extends InstitutionType("affiliate", Some(NonDepository))

  // FIXME: These are temporary InstitutionType(s) used for testing.  They will be replaced
  //        by real ones once we know what they are. :)
  case object NonDepositInstType extends InstitutionType("test-non-depository", Some(NonDepository))
  case object NoDepositTypeInstType extends InstitutionType("test-no-depository-type", None)
}
