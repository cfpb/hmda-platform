package hmda.publication.reports.util

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.publication.reports.DispositionEnum._
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

    def dispositionEnum: DispositionEnum

    def calculateDisposition[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegister, NotUsed]): Future[Disposition] = {
      val loansFiltered = larSource.filter(filter)
      val loanCountF = count(loansFiltered)
      val incomeF = sum(loansFiltered, incomeSum)
      for {
        count <- loanCountF
        income <- incomeF
      } yield {
        Disposition(dispositionEnum, count, income)
      }
    }

    private def incomeSum(lar: LoanApplicationRegister): Int = Try(lar.applicant.income.toInt).getOrElse(0)
  }

  object ReceivedDisp extends DispositionType {
    override def filter(lar: LoanApplicationRegister): Boolean =
      lar.actionTakenType == 1 || lar.actionTakenType == 2 || lar.actionTakenType == 3 ||
        lar.actionTakenType == 4 || lar.actionTakenType == 5
    override def dispositionEnum: DispositionEnum = ApplicationReceived
  }
  object OriginatedDisp extends DispositionType {
    override def filter(lar: LoanApplicationRegister): Boolean = lar.actionTakenType == 1
    override def dispositionEnum: DispositionEnum = LoansOriginated
  }
  object ApprovedButNotAcceptedDisp extends DispositionType {
    override def filter(lar: LoanApplicationRegister): Boolean = lar.actionTakenType == 2
    override def dispositionEnum: DispositionEnum = ApprovedButNotAccepted
  }
  object DeniedDisp extends DispositionType {
    override def filter(lar: LoanApplicationRegister): Boolean = lar.actionTakenType == 3
    override def dispositionEnum: DispositionEnum = ApplicationsDenied
  }
  object WithdrawnDisp extends DispositionType {
    override def filter(lar: LoanApplicationRegister): Boolean = lar.actionTakenType == 4
    override def dispositionEnum: DispositionEnum = ApplicationsWithdrawn
  }
  object ClosedDisp extends DispositionType {
    override def filter(lar: LoanApplicationRegister): Boolean = lar.actionTakenType == 5
    override def dispositionEnum: DispositionEnum = ClosedForIncompleteness
  }
  object PurchasedDisp extends DispositionType {
    override def filter(lar: LoanApplicationRegister): Boolean = lar.actionTakenType == 6
    override def dispositionEnum: DispositionEnum = LoanPurchased
  }
  object PreapprovalDeniedDisp extends DispositionType {
    override def filter(lar: LoanApplicationRegister): Boolean = lar.actionTakenType == 7
    override def dispositionEnum: DispositionEnum = PreapprovalDenied
  }
  object PreapprovalApprovedDisp extends DispositionType {
    override def filter(lar: LoanApplicationRegister): Boolean = lar.actionTakenType == 8
    override def dispositionEnum: DispositionEnum = PreapprovalApprovedButNotAccepted
  }
  object FannieMaeDisp extends DispositionType {
    override def filter(lar: LoanApplicationRegister): Boolean = lar.purchaserType == 1
    override def dispositionEnum: DispositionEnum = FannieMae
  }
  object GinnieMaeDisp extends DispositionType {
    override def filter(lar: LoanApplicationRegister): Boolean = lar.purchaserType == 2
    override def dispositionEnum: DispositionEnum = GinnieMae
  }
  object FreddieMacDisp extends DispositionType {
    override def filter(lar: LoanApplicationRegister): Boolean = lar.purchaserType == 3
    override def dispositionEnum: DispositionEnum = FreddieMac
  }
  object FarmerMacDisp extends DispositionType {
    override def filter(lar: LoanApplicationRegister): Boolean = lar.purchaserType == 4
    override def dispositionEnum: DispositionEnum = FarmerMac
  }
  object PrivateSecuritizationDisp extends DispositionType {
    override def filter(lar: LoanApplicationRegister): Boolean = lar.purchaserType == 5
    override def dispositionEnum: DispositionEnum = PrivateSecuritization
  }
  object CommercialBankDisp extends DispositionType {
    override def filter(lar: LoanApplicationRegister): Boolean = lar.purchaserType == 6
    override def dispositionEnum: DispositionEnum = CommercialBank
  }
  object FinanceCompanyDisp extends DispositionType {
    override def filter(lar: LoanApplicationRegister): Boolean = lar.purchaserType == 7
    override def dispositionEnum: DispositionEnum = FinanceCompany
  }
  object AffiliateDisp extends DispositionType {
    override def filter(lar: LoanApplicationRegister): Boolean = lar.purchaserType == 8
    override def dispositionEnum: DispositionEnum = Affiliate
  }
  object OtherPurchaserDisp extends DispositionType {
    override def filter(lar: LoanApplicationRegister): Boolean = lar.purchaserType == 9
    override def dispositionEnum: DispositionEnum = OtherPurchaser
  }
}
