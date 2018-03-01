package hmda.publication.reports.util

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.publication.reports._
import hmda.publication.reports.{ AS, EC, MAT }
import hmda.util.SourceUtils

import scala.concurrent.Future

sealed abstract class DispositionType(
    val value: String,
    val filter: LoanApplicationRegister => Boolean
) extends SourceUtils {

  def calculateValueDisposition[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegister, NotUsed]): Future[ValueDisposition] = {
    val loansFiltered = larSource.filter(filter)
    val loanCountF = count(loansFiltered)
    val totalValueF = sum(loansFiltered, loanAmount)
    for {
      count <- loanCountF
      total <- totalValueF
    } yield {
      ValueDisposition(value, count, total)
    }
  }

  def calculatePercentageDisposition[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegister, NotUsed]): Future[PercentageDisposition] = {
    val loansFiltered = larSource.filter(filter)
    val loanCountF = count(loansFiltered)
    loanCountF.map { count =>
      PercentageDisposition(value, count, 0)
    }
  }

  private def loanAmount(lar: LoanApplicationRegister): Int = lar.loan.amount
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
    _.denial.reason1 == "1"
  )
  case object EmploymentHistory extends DispositionType(
    "Employment History",
    _.denial.reason1 == "2"
  )
  case object CreditHistory extends DispositionType(
    "Credit History",
    _.denial.reason1 == "3"
  )
  case object Collateral extends DispositionType(
    "Collateral",
    _.denial.reason1 == "4"
  )
  case object InsufficientCash extends DispositionType(
    "Insufficient Cash",
    _.denial.reason1 == "5"
  )
  case object UnverifiableInformation extends DispositionType(
    "Unverifiable Information",
    _.denial.reason1 == "6"
  )
  case object CreditAppIncomplete extends DispositionType(
    "Credit App. Incomplete",
    _.denial.reason1 == "7"
  )
  case object MortgageInsuranceDenied extends DispositionType(
    "Mortgage Insurance Denied",
    _.denial.reason1 == "8"
  )
  case object OtherDenialReason extends DispositionType(
    "Other",
    _.denial.reason1 == "9"
  )
  case object TotalDenied extends DispositionType(
    "Total",
    _.denial.reason1 != ""
  )

  //////////////////////////////////
  // Table 1/2 Dispositions
  //////////////////////////////////
  case object FHA extends DispositionType(
    "FHA, FSA/RHS & VA (A)",
    lar => (lar.loan.propertyType == 1 || lar.loan.propertyType == 2)
      && (lar.loan.purpose == 1)
      && (2 to 4).contains(lar.loan.loanType)
  )
  case object Conventional extends DispositionType(
    "Conventional (B)",
    lar => (lar.loan.propertyType == 1 || lar.loan.propertyType == 2)
      && (lar.loan.purpose == 1)
      && (lar.loan.loanType == 1)
  )
  case object Refinancings extends DispositionType(
    "Refinancings (C)",
    lar => (lar.loan.propertyType == 1 || lar.loan.propertyType == 2)
      && (lar.loan.purpose == 3)
  )
  case object HomeImprovementLoans extends DispositionType(
    "Home Improvement Loans (D)",
    lar => (lar.loan.propertyType == 1 || lar.loan.propertyType == 2)
      && (lar.loan.purpose == 2)
  )
  case object LoansForFiveOrMore extends DispositionType(
    "Loans on Dwellings For 5 or More Families (E)",
    lar => lar.loan.propertyType == 3
  )
  case object NonoccupantLoans extends DispositionType(
    "Nonoccupant Loans (F)",
    lar => lar.loan.occupancy == 2
  )
  case object ManufacturedHomeDwellings extends DispositionType(
    "Loans On Manufactured Home Dwellings (G)",
    lar => lar.loan.propertyType == 2
  )
    
  //////////////////////////////////
  // Preapprovals Dispositions
  //////////////////////////////////

  case object PreapprovalsToOriginations extends DispositionType(
    "Preapprovals reasulting in originations",
    lar => lar.preapprovals == 1 && lar.actionTakenType == 1
  )
  case object PreapprovalsNotAccepted extends DispositionType(
    "Preapprovals approved but not accepted",
    _.actionTakenType == 8
  )
  case object PreApprovalsDenied extends DispositionType(
    "Preapprovals denied",
    _.actionTakenType == 7
  )
}
