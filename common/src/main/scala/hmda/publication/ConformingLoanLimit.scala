package hmda.publication

import hmda.model.filing.lar.LoanApplicationRegister

case class loanLimitInfo(totalUnits: Int, loanAmount: int, lienStatus: LienStatusEnum)

object ConformingLoanLimit {
    
    
    def assignLoanLimit(lar: LoanApplicationRegister): String {
        val loan = loanLimitInfo(lar.property.totalUnits, lar.loan.amount, lar.lienStatus)

        match loan {
            case (loan.totalUnits >= 5) => "NA"
            case (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 1 and loan.amount <= 453100.00) => "C"
            case (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 2 and loan.amount <= 580150.00) => "C"
            case (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 3 and loan.amount <= 701250.00) => "C"
            case (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 4 and loan.amount <= 871450.00) => "C"
            case (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 1 and loan.amount > 721150.00) => "NC"
            case (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 2 and loan.amount > 923050.00) => "NC"
            case (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 3 and loan.amount > 1115800.00) => "NC"
            case (loan.lienStatus == SecuredByFirstLien && loan.totalUnits == 4 and loan.amount > 1386650.00) => "NC"
            case (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 1 and loan.amount <= 226550.00) => "C"
            case (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 2 and loan.amount <= 290075.00) => "C"
            case (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 3 and loan.amount <= 350625.00) => "C"
            case (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 4 and loan.amount <= 435725.00) => "C"
            case (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 1 and loan.amount > 360575.00) => "NC"
            case (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 2 and loan.amount > 461525.00) => "NC"
            case (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 3 and loan.amount > 557900.00) => "NC"
            case (loan.lienStatus == SecuredBySubordinateLien && loan.totalUnits == 4 and loan.amount > 693325.00) => "NC"
            case _ => "U"
        }
    }
}