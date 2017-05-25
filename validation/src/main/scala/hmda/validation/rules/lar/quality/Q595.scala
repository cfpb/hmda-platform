package hmda.validation.rules.lar.quality

import hmda.census.model.RssdToMsa
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.model.institution.InstitutionType.{ Affiliate, Bank, CreditUnion, SavingsAndLoan }
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.Result
import hmda.validation.rules.{ EditCheck, IfInstitutionPresentIn }

class Q595 private (institution: Institution) extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q595"

  override def apply(lar: LoanApplicationRegister): Result = {
    val rssd = institution.id
    // pull in list of MSA/MD from file based on rssd
    val msaList = RssdToMsa.map.getOrElse(rssd, List())
    val applicableActionTakenTypes = List(1, 2, 3, 4, 5, 7, 8)
    val applicableInstitutionTypes = List(Bank, SavingsAndLoan, CreditUnion, Affiliate)

    when((lar.actionTakenType is containedIn(applicableActionTakenTypes))
      and (institution.institutionType is containedIn(applicableInstitutionTypes))) {

      (lar.geography.msa is containedIn(msaList)) or (lar.geography.msa is "NA")
    }
  }
}

object Q595 {
  def inContext(context: ValidationContext): EditCheck[LoanApplicationRegister] = {
    IfInstitutionPresentIn(context) { new Q595(_) }
  }
}
