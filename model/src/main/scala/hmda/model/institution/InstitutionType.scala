package hmda.model.institution

import enumeratum.{ Enum, EnumEntry }

sealed trait DepositoryType extends EnumEntry

object DepositoryType extends Enum[DepositoryType] {

  val values = findValues

  case object Depository extends DepositoryType
  case object NonDepository extends DepositoryType
}

sealed abstract class InstitutionType(val name: String, val depositoryType: Option[DepositoryType]) extends EnumEntry

object InstitutionType extends Enum[InstitutionType] {

  import DepositoryType._

  val values = findValues

  //FIXME: None of these are officially confirmed "institution types"
  case object Bank extends InstitutionType("bank", Some(Depository))
  case object CreditUnion extends InstitutionType("credit-union", Some(Depository))
  case object SavingsAndLoan extends InstitutionType("savings-and-loan", Some(Depository))
  case object MortgateBankingSubsidiary extends InstitutionType("mortgate-banking-subsidiary", Some(NonDepository))

}

