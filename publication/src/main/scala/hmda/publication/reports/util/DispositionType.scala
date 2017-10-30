package hmda.publication.reports.util

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.publication.reports.ActionTakenTypeEnum._
import hmda.model.publication.reports._
import hmda.publication.reports.{ AS, EC, MAT }
import hmda.util.SourceUtils

import scala.concurrent.Future
import scala.util.Try

object DispositionType {

  val byName: Map[String, DispositionType] = {
    Map(
      "received" -> ReceivedDisp,
      "originated" -> OriginatedDisp,
      "approvedbutnotaccepted" -> ApprovedButNotAcceptedDisp,
      "denied" -> DeniedDisp,
      "withdrawn" -> WithdrawnDisp,
      "closed" -> ClosedDisp,
      "purchased" -> PurchasedDisp,
      "preapprovaldenied" -> PreapprovalDeniedDisp,
      "preapprovalapproved" -> PreapprovalApprovedDisp
    )
  }

  sealed trait DispositionType extends SourceUtils {
    def filter(lar: LoanApplicationRegister): Boolean

    def actionTaken: ActionTakenTypeEnum

    def calculateDisposition[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegister, NotUsed]): Future[Disposition] = {
      val loansFiltered = larSource.filter(filter)
      val loanCountF = count(loansFiltered)
      val incomeF = sum(loansFiltered, incomeSum)
      for {
        count <- loanCountF
        income <- incomeF
      } yield {
        Disposition(actionTaken, count, income)
      }
    }

    private def incomeSum(lar: LoanApplicationRegister): Int = Try(lar.applicant.income.toInt).getOrElse(0)
  }

  object ReceivedDisp extends DispositionType {
    override def filter(lar: LoanApplicationRegister): Boolean =
      lar.actionTakenType == 1 || lar.actionTakenType == 2 || lar.actionTakenType == 3 ||
        lar.actionTakenType == 4 || lar.actionTakenType == 5
    override def actionTaken: ActionTakenTypeEnum = ApplicationReceived
  }
  object OriginatedDisp extends DispositionType {
    override def filter(lar: LoanApplicationRegister): Boolean = lar.actionTakenType == 1
    override def actionTaken: ActionTakenTypeEnum = LoansOriginated
  }
  object ApprovedButNotAcceptedDisp extends DispositionType {
    override def filter(lar: LoanApplicationRegister): Boolean = lar.actionTakenType == 2
    override def actionTaken: ActionTakenTypeEnum = ApprovedButNotAccepted
  }
  object DeniedDisp extends DispositionType {
    override def filter(lar: LoanApplicationRegister): Boolean = lar.actionTakenType == 3
    override def actionTaken: ActionTakenTypeEnum = ApplicationsDenied
  }
  object WithdrawnDisp extends DispositionType {
    override def filter(lar: LoanApplicationRegister): Boolean = lar.actionTakenType == 4
    override def actionTaken: ActionTakenTypeEnum = ApplicationsWithdrawn
  }
  object ClosedDisp extends DispositionType {
    override def filter(lar: LoanApplicationRegister): Boolean = lar.actionTakenType == 5
    override def actionTaken: ActionTakenTypeEnum = ClosedForIncompleteness
  }
  object PurchasedDisp extends DispositionType {
    override def filter(lar: LoanApplicationRegister): Boolean = lar.actionTakenType == 6
    override def actionTaken: ActionTakenTypeEnum = LoanPurchased
  }
  object PreapprovalDeniedDisp extends DispositionType {
    override def filter(lar: LoanApplicationRegister): Boolean = lar.actionTakenType == 7
    override def actionTaken: ActionTakenTypeEnum = PreapprovalDenied
  }
  object PreapprovalApprovedDisp extends DispositionType {
    override def filter(lar: LoanApplicationRegister): Boolean = lar.actionTakenType == 8
    override def actionTaken: ActionTakenTypeEnum = PreapprovalApprovedButNotAccepted
  }

}
