package hmda.publication.reports.util

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.publication.reports._
import hmda.publication.reports.{ AS, EC, MAT }
import hmda.util.SourceUtils

import scala.concurrent.Future
import scala.util.Try

sealed abstract class DispositionType(
    val value: String,
    val filter: LoanApplicationRegister => Boolean
) extends SourceUtils {

  def calculateValueDisposition[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegister, NotUsed]): Future[Disposition] = {
    val loansFiltered = larSource.filter(filter)
    val loanCountF = count(loansFiltered)
    val incomeF = sum(loansFiltered, incomeSum)
    for {
      count <- loanCountF
      income <- incomeF
    } yield {
      Disposition(value, count, income)
    }
  }

  private def incomeSum(lar: LoanApplicationRegister): Int = Try(lar.applicant.income.toInt).getOrElse(0)
}

object DispositionType {

  val byName: Map[String, DispositionType] = {
    Map(
      "received" -> ApplicationReceived,
      "originated" -> LoansOriginated,
      "approvedbutnotaccepted" -> ApprovedButNotAccepted,
      "denied" -> ApplicationsDenied,
      "withdrawn" -> ApplicationsWithdrawn,
      "closed" -> ClosedForIncompleteness,
      "purchased" -> LoanPurchased,
      "preapprovaldenied" -> PreapprovalDenied,
      "preapprovalapproved" -> PreapprovalApprovedButNotAccepted
    )
  }

  //////////////////////////////////
  // Action Taken Type Dispositions
  //////////////////////////////////

  case object ApplicationReceived extends DispositionType(
    "Applications Received",
    lar => lar.actionTakenType == 1 || lar.actionTakenType == 2 || lar.actionTakenType == 3 ||
      lar.actionTakenType == 4 || lar.actionTakenType == 5
  )
  case object LoansOriginated extends DispositionType(
    "Loans Originated",
    _.actionTakenType == 1
  )
  case object ApprovedButNotAccepted extends DispositionType(
    "Apps. Approved But Not Accepted",
    _.actionTakenType == 2
  )
  case object ApplicationsDenied extends DispositionType(
    "Applications Denied",
    _.actionTakenType == 3
  )
  case object ApplicationsWithdrawn extends DispositionType(
    "Applications Withdrawn",
    _.actionTakenType == 4
  )
  case object ClosedForIncompleteness extends DispositionType(
    "Files Closed for Incompleteness",
    _.actionTakenType == 5
  )
  case object LoanPurchased extends DispositionType(
    "Loan Purchased by your Institution",
    _.actionTakenType == 6
  )
  case object PreapprovalDenied extends DispositionType(
    "Preapproval Request Denied by Financial Institution",
    _.actionTakenType == 7
  )
  case object PreapprovalApprovedButNotAccepted extends DispositionType(
    "Preapproval Request Approved but not Accepted by Financial Institution",
    _.actionTakenType == 8
  )

  //////////////////////////////////
  // Purchaser Type Dispositions
  //////////////////////////////////

  case object FannieMae extends DispositionType(
    "Fannie Mae",
    _.purchaserType == 1
  )
  case object GinnieMae extends DispositionType(
    "Ginnie Mae",
    _.purchaserType == 2
  )
  case object FreddieMac extends DispositionType(
    "FreddieMac",
    _.purchaserType == 3
  )
  case object FarmerMac extends DispositionType(
    "FarmerMac",
    _.purchaserType == 4
  )
  case object PrivateSecuritization extends DispositionType(
    "Private Securitization",
    _.purchaserType == 5
  )
  case object CommercialBank extends DispositionType(
    "Commercial bank, savings bank or association",
    _.purchaserType == 6
  )
  case object FinanceCompany extends DispositionType(
    "Life insurance co., credit union, finance co.",
    _.purchaserType == 7
  )
  case object Affiliate extends DispositionType(
    "Affiliate institution",
    _.purchaserType == 8
  )
  case object OtherPurchaser extends DispositionType(
    "Other",
    _.purchaserType == 9
  )

  //////////////////////////////////
  // Denial Reason Dispositions
  //////////////////////////////////

  case object DebtToIncomeRatio extends DispositionType(
    "Debt-to-Income Ratio",
    lar =>
      lar.denial.reason1 == "1" ||
        lar.denial.reason2 == "1" ||
        lar.denial.reason3 == "1"
  )
  case object EmploymentHistory extends DispositionType(
    "Employment History",
    lar =>
      lar.denial.reason1 == "2" ||
        lar.denial.reason2 == "2" ||
        lar.denial.reason3 == "2"
  )
  case object CreditHistory extends DispositionType(
    "Credit History",
    lar =>
      lar.denial.reason1 == "3" ||
        lar.denial.reason2 == "3" ||
        lar.denial.reason3 == "3"
  )
  case object Collateral extends DispositionType(
    "Collateral",
    lar =>
      lar.denial.reason1 == "4" ||
        lar.denial.reason2 == "4" ||
        lar.denial.reason3 == "4"
  )
  case object InsufficientCash extends DispositionType(
    "Insufficient Cash",
    lar =>
      lar.denial.reason1 == "5" ||
        lar.denial.reason2 == "5" ||
        lar.denial.reason3 == "5"
  )
  case object UnverifiableInformation extends DispositionType(
    "Unverifiable Information",
    lar =>
      lar.denial.reason1 == "6" ||
        lar.denial.reason2 == "6" ||
        lar.denial.reason3 == "6"
  )
  case object CreditAppIncomplete extends DispositionType(
    "Credit App. Incomplete",
    lar =>
      lar.denial.reason1 == "7" ||
        lar.denial.reason2 == "7" ||
        lar.denial.reason3 == "7"
  )
  case object MortgageInsuranceDenied extends DispositionType(
    "Mortgage Insurance Denied",
    lar =>
      lar.denial.reason1 == "8" ||
        lar.denial.reason2 == "8" ||
        lar.denial.reason3 == "8"
  )
  case object OtherDenialReason extends DispositionType(
    "Other",
    lar =>
      lar.denial.reason1 == "9" ||
        lar.denial.reason2 == "9" ||
        lar.denial.reason3 == "9"
  )
  case object TotalDenied extends DispositionType(
    "Total",
    lar =>
      lar.denial.reason1 != "" ||
        lar.denial.reason2 != "" ||
        lar.denial.reason3 != ""
  )

}
