package hmda.publication.reports.disclosure

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.model.publication.reports.ValueDisposition
import hmda.model.publication.reports.EthnicityEnum._
import hmda.model.publication.reports.GenderEnum._
import hmda.model.publication.reports.ApplicantIncomeEnum._
import hmda.model.publication.reports.MinorityStatusEnum._
import hmda.model.publication.reports.RaceEnum._
import hmda.publication.reports._
import hmda.publication.reports.util.DispositionType._
import hmda.publication.reports.util.EthnicityUtil.filterEthnicity
import hmda.publication.reports.util.GenderUtil.filterGender
import hmda.publication.reports.util.MinorityStatusUtil.filterMinorityStatus
import hmda.publication.reports.util.RaceUtil.filterRace
import hmda.publication.reports.util.ReportUtil._
import hmda.publication.reports.util.ReportsMetaDataLookup

import scala.concurrent.Future

object A1 extends DisclosureA {
  val reportId = "A1"
  def filters(lar: LoanApplicationRegister, msa: Int): Boolean = {
    (1 to 4).contains(lar.loan.loanType) &&
      lar.loan.propertyType == 1 &&
      lar.geography.msa.toInt == msa
  }
}

object A1W extends DisclosureA {
  val reportId = "A1W"
  def filters(lar: LoanApplicationRegister, msa: Int): Boolean = {
    (1 to 4).contains(lar.loan.loanType) &&
      lar.loan.propertyType == 1
  }
}

trait DisclosureA extends DisclosureReport {
  val reportId: String
  def filters(lar: LoanApplicationRegister, msa: Int): Boolean

  val dispositions = List(ApplicationReceived, LoansOriginated, ApprovedButNotAccepted,
    ApplicationsDenied, ApplicationsWithdrawn, ClosedForIncompleteness)

  def generate[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegister, NotUsed],
    fipsCode: Int,
    institution: Institution
  ): Future[DisclosureReportPayload] = {

    val metaData = ReportsMetaDataLookup.values(reportId)

    val lars = larSource
      .filter(lar => filters(lar, fipsCode))

    val msa = msaReport(fipsCode.toString).toJsonFormat
    val reportDate = formattedCurrentDate
    val yearF = calculateYear(larSource)

    for {
      year <- yearF

      received <- loanTypes(lars.filter(lar => (1 to 5).contains(lar.actionTakenType)))
      originiated <- loanTypes(lars.filter(lar => lar.actionTakenType == 1))
      appNotAcc <- loanTypes(lars.filter(lar => lar.actionTakenType == 2))
      denied <- loanTypes(lars.filter(lar => lar.actionTakenType == 3))
      withdrawn <- loanTypes(lars.filter(lar => lar.actionTakenType == 4))
      closed <- loanTypes(lars.filter(lar => lar.actionTakenType == 5))
      preapproval <- loanTypes(lars.filter(lar => lar.actionTakenType == 1 && lar.preapprovals ==1))
      sold <- loanTypes(lars.filter(lar => (1 to 9).contains(lar.purchaserType)))
    } yield {
      val report = s"""
         |{
         |    "respondentId": "${institution.respondentId}",
         |    "institutionName": "${institution.respondent.name}",
         |    "table": "${metaData.reportTable}",
         |    "type": "Disclosure",
         |    "description": "${metaData.description}",
         |    "year": "$year",
         |    "reportDate": "$reportDate",
         |    "msa": $msa,,
         |    "dispositions": [
         |        {
         |            "disposition": "Applications Received",
         |            "loantypes": $received
         |        },
         |        {
         |            "disposition": "Loans Originated",
         |            "loantypes": $originiated
         |        },
         |        {
         |            "disposition": "Apps. Approved But Not Accepted",
         |            "loantypes": $appNotAcc
         |        },
         |        {
         |            "disposition": "Applications Denied",
         |            "loantypes": $denied
         |        },
         |        {
         |            "disposition": "Applications Withdrawn",
         |            "loantypes": $withdrawn
         |        },
         |        {
         |            "disposition": "Files Closed For Incompleteness",
         |            "loantypes": $closed
         |        },
         |        {
         |            "disposition": "Preapprovals Resulting in Originations",
         |            "loantypes": $preapproval
         |        },
         |        {
         |            "disposition": "Loans Sold",
         |            "loantypes": $sold
         |        }
         |    ]
         |}
       """.stripMargin

      DisclosureReportPayload(metaData.reportTable, fipsCode.toString, report)
    }
  }

  private def loanTypes[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegister, NotUsed]): Future[String] = {
    for {
      conv <- purposesOutput(larSource.filter(lar => lar.loan.loanType == 1))
      fha <- purposesOutput(larSource.filter(lar => lar.loan.loanType == 2))
      va <- purposesOutput(larSource.filter(lar => lar.loan.loanType == 3))
      fsa <- purposesOutput(larSource.filter(lar => lar.loan.loanType == 4))
    } yield {
      s"""
         |[
         |  {
         |    "loantype": "Conventional",
         |    "purposes": $conv
         |  },
         |  {
         |    "loantype": "FHA",
         |    "purposes": $fha
         |  },
         |  {
         |    "loantype": "VA",
         |    "purposes": $va
         |  },
         |  {
         |    "loantype": "FSA/RHS",
         |    "purposes": $fsa
         |  }
         |]
     """.stripMargin
    }
  }

  private def purposesOutput[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegister, NotUsed]): Future[String] = {
    for {
      homePurchaseFirst <- count(larSource.filter(lar => lar.lienStatus == 1 && lar.loan.purpose == 1))
      homePurchaseJunior <- count(larSource.filter(lar => lar.lienStatus == 2 && lar.loan.purpose == 1))
      refinanceFirst <- count(larSource.filter(lar => lar.lienStatus == 1 && lar.loan.purpose == 3))
      refinanceJunior <- count(larSource.filter(lar => lar.lienStatus == 2 && lar.loan.purpose == 3))
      homeImprovementFirst <- count(larSource.filter(lar => lar.lienStatus == 1 && lar.loan.purpose == 2))
      homeImprovementJunior <- count(larSource.filter(lar => lar.lienStatus == 2 && lar.loan.purpose == 2))
      homeImprovementNo <- count(larSource.filter(lar => lar.lienStatus == 3 && lar.loan.purpose == 2))
    } yield {
      s"""
         |[
         |  {
         |    "purpose": "Home Purchase",
         |    "firstliencount": $homePurchaseFirst,
         |    "juniorliencount": $homePurchaseJunior
         |  },
         |  {
         |    "purpose": "Refinance",
         |    "firstliencount": $refinanceFirst,
         |    "juniorliencount": $refinanceJunior
         |  },
         |  {
         |    "purpose": "Home Improvement",
         |    "firstliencount": $homeImprovementFirst,
         |    "juniorliencount": $homeImprovementJunior,
         |    "noliencount": $homeImprovementNo
         |  }
         |]
     """.stripMargin
    }
  }

}
