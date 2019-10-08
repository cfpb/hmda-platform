package hmda.parser.derivedFields

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._

package object LoanProductTypeCategorization {

  def assignLoanProductTypeCategorization(lar: LoanApplicationRegister): String =
    if (lar.loan.loanType == Conventional && lar.lienStatus == SecuredByFirstLien)
      "Conventional:First Lien"
    else if (lar.loan.loanType == FHAInsured && lar.lienStatus == SecuredByFirstLien)
      "FHA:First Lien"
    else if (lar.loan.loanType == VAGuaranteed && lar.lienStatus == SecuredByFirstLien)
      "VA:First Lien"
    else if (lar.loan.loanType == RHSOrFSAGuaranteed && lar.lienStatus == SecuredByFirstLien)
      "FSA/RHS:First Lien"
    else if (lar.loan.loanType == Conventional && lar.lienStatus == SecuredBySubordinateLien)
      "Conventional:Subordinate Lien"
    else if (lar.loan.loanType == FHAInsured && lar.lienStatus == SecuredBySubordinateLien)
      "FHA:Subordinate Lien"
    else if (lar.loan.loanType == VAGuaranteed && lar.lienStatus == SecuredBySubordinateLien)
      "VA:Subordinate Lien"
    else if (lar.loan.loanType == RHSOrFSAGuaranteed && lar.lienStatus == SecuredBySubordinateLien)
      "FSA/RHS:Subordinate Lien"
    else
      "N/A"
}
