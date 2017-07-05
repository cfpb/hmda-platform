package hmda.publication.reports.disclosure

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.publication.reports.{ActionTakenTypeEnum, Disposition}
import hmda.publication.reports.{AS, EC, MAT}
import hmda.util.SourceUtils
import scala.util.Try


object DispositionGenerator extends SourceUtils {
  def calculateDispositions[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegister, NotUsed]) = {
    def incomeSum(lar: LoanApplicationRegister): Int = Try(lar.applicant.income.toInt).getOrElse(0)

    val loansOriginated = larSource.filter(lar => lar.actionTakenType == 1)
    val loansOriginatedCountF = count(loansOriginated)
    val incomeLoansOriginatedF = sum(loansOriginated, incomeSum)

    val loanApprovedButNotAccepted = larSource.filter(lar => lar.actionTakenType == 2)
    val loanApprovedButNotAcceptedCountF = count(loanApprovedButNotAccepted)
    val incomeApprovedButNotAcceptedF = sum(loanApprovedButNotAccepted, incomeSum)

    val loansDenied = larSource.filter(lar => lar.actionTakenType == 3)
    val loansDeniedCountF = count(loansDenied)
    val incomeLoansDeniedF = sum(loansDenied, incomeSum)

    val loansWithdrawn = larSource.filter(lar => lar.actionTakenType == 4)
    val loansWithdrawnCountF = count(loansWithdrawn)
    val incomeLoansWithdrawnF = sum(loansWithdrawn, incomeSum)

    val loansClosed = larSource.filter(lar => lar.actionTakenType == 5)
    val loansClosedCountF = count(loansClosed)
    val incomeLoansClosedF = sum(loansClosed, incomeSum)

    val loansPurchased = larSource.filter(lar => lar.actionTakenType == 6)
    val loansPurchasedCountF = count(loansPurchased)
    val incomeLoansPurchasedF = sum(loansPurchased, incomeSum)

    val preapprovalDenied = larSource.filter(lar => lar.actionTakenType == 7)
    val preapprovalDeniedCountF = count(preapprovalDenied)
    val incomePreapprovalDeniedF = sum(preapprovalDenied, incomeSum)

    val preapprovalAccepted = larSource.filter(lar => lar.actionTakenType == 8)
    val preapprovalAcceptedCountF = count(preapprovalAccepted)
    val incomePreapprovalAcceptedF = sum(preapprovalAccepted, incomeSum)

    for {
      loansOriginatedCount <- loansOriginatedCountF
      incomeLoansOriginated <- incomeLoansOriginatedF
      loanApprovedButNotAcceptedCount <- loanApprovedButNotAcceptedCountF
      incomeApprovedButNotAccepted <- incomeApprovedButNotAcceptedF
      loansDeniedCount <- loansDeniedCountF
      incomeLoansDenied <- incomeLoansDeniedF
      loansWithdrawnCount <- loansWithdrawnCountF
      incomeLoansWithdrawn <- incomeLoansWithdrawnF
      loansClosedCount <- loansClosedCountF
      incomeLoansClosed <- incomeLoansClosedF
      loansPurchasedCount <- loansPurchasedCountF
      incomeLoansPurchased <- incomeLoansPurchasedF
      preapprovalDeniedCount <- preapprovalDeniedCountF
      incomePreapprovalDenied <- incomePreapprovalDeniedF
      preapprovalAcceptedCount <- preapprovalAcceptedCountF
      incomePreapprovalAccepted <- incomePreapprovalAcceptedF
      receivedCount = loansOriginatedCount + loanApprovedButNotAcceptedCount + loansDeniedCount + loansWithdrawnCount +
        loansClosedCount + loansPurchasedCount + preapprovalDeniedCount + preapprovalAcceptedCount
      incomeReceivedCount = incomeLoansOriginated + incomeApprovedButNotAccepted + incomeLoansDenied +
        incomeLoansWithdrawn + incomeLoansClosed + incomeLoansPurchased + incomePreapprovalDenied + incomePreapprovalAccepted
    } yield {
      val applicationsReceived = Disposition(ActionTakenTypeEnum.ApplicationReceived, receivedCount, incomeReceivedCount)
      val loansOriginatedDisp = Disposition(ActionTakenTypeEnum.LoansOriginated, loansOriginatedCount, incomeLoansOriginated)
      val loansApprovedButNotAcceptedDisp = Disposition(ActionTakenTypeEnum.ApprovedButNotAccepted, loanApprovedButNotAcceptedCount, incomeApprovedButNotAccepted)
      val loansDenied = Disposition(ActionTakenTypeEnum.ApplicationsDenied, loansDeniedCount, incomeLoansDenied)
      val loansWithdrawn = Disposition(ActionTakenTypeEnum.ApplicationsWithdrawn, loansWithdrawnCount, incomeLoansWithdrawn)
      val loansClosed = Disposition(ActionTakenTypeEnum.ClosedForIncompleteness, loansClosedCount, incomeLoansClosed)
      val loansPurchased = Disposition(ActionTakenTypeEnum.LoanPurchased, loansPurchasedCount, incomeLoansPurchased)
      val preapprovalDenied = Disposition(ActionTakenTypeEnum.PreapprovalDenied, preapprovalDeniedCount, incomePreapprovalDenied)
      val preapprovalAccepted = Disposition(ActionTakenTypeEnum.PreapprovalApprovedButNotAccepted, preapprovalAcceptedCount, incomePreapprovalAccepted)
      List(
        applicationsReceived,
        loansOriginatedDisp,
        loansApprovedButNotAcceptedDisp,
        loansDenied,
        loansWithdrawn,
        loansClosed,
        loansPurchased,
        preapprovalDenied,
        preapprovalAccepted
      )
    }
  }
}
