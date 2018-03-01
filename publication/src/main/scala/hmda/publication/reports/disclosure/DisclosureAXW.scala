package hmda.publication.reports.disclosure

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.publication.reports._
import hmda.publication.reports.util.ReportUtil._
import hmda.publication.reports.util.LoanTypeUtil._
import hmda.publication.reports.util.ReportsMetaDataLookup

import scala.concurrent.Future

object A1W extends AXW {
  val reportId = "DA1W"
  def filters(lar: LoanApplicationRegister): Boolean = {
    (1 to 4).contains(lar.loan.loanType) &&
      lar.loan.propertyType == 1
  }
}

object A2W extends AXW {
  val reportId = "DA2W"
  def filters(lar: LoanApplicationRegister): Boolean = {
    (1 to 4).contains(lar.loan.loanType) &&
      lar.loan.propertyType == 2
  }
}

object A3W extends AXW {
  val reportId = "DA3W"
  def filters(lar: LoanApplicationRegister): Boolean = {
    (1 to 4).contains(lar.loan.loanType) &&
      lar.loan.propertyType == 3
  }
}

trait AXW extends DisclosureReport {
  val reportId: String
  def filters(lar: LoanApplicationRegister): Boolean

  def generate[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegister, NotUsed],
    fipsCode: Int,
    institution: Institution
  ): Future[DisclosureReportPayload] = {

    val metaData = ReportsMetaDataLookup.values(reportId)

    val lars = larSource
      .filter(filters)

    val reportDate = formattedCurrentDate
    val yearF = calculateYear(larSource)

    for {
      year <- yearF

      received <- loanTypes(lars.filter(lar => List(1, 2, 3, 4, 5, 7, 8).contains(lar.actionTakenType)))
      originiated <- loanTypes(lars.filter(lar => lar.actionTakenType == 1))
      appNotAcc <- loanTypes(lars.filter(lar => lar.actionTakenType == 2))
      appDenied <- loanTypes(lars.filter(lar => lar.actionTakenType == 3))
      withdrawn <- loanTypes(lars.filter(lar => lar.actionTakenType == 4))
      closed <- loanTypes(lars.filter(lar => lar.actionTakenType == 5))
      preDenied <- loanTypes(lars.filter(lar => lar.actionTakenType == 7))
      preAppNotAcc <- loanTypes(lars.filter(lar => lar.actionTakenType == 8))
      preapproval <- loanTypes(lars.filter(lar => lar.actionTakenType == 1 && lar.preapprovals == 1))
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
                      |    "dispositions": [
                      |        {
                      |            "disposition": "Applications Received",
                      |            "loanTypes": $received
                      |        },
                      |        {
                      |            "disposition": "Loans Originated",
                      |            "loanTypes": $originiated
                      |        },
                      |        {
                      |            "disposition": "Apps. Approved But Not Accepted",
                      |            "loanTypes": $appNotAcc
                      |        },
                      |        {
                      |            "disposition": "Applications Denied",
                      |            "loanTypes": $appDenied
                      |        },
                      |        {
                      |            "disposition": "Applications Withdrawn",
                      |            "loanTypes": $withdrawn
                      |        },
                      |        {
                      |            "disposition": "Files Closed For Incompleteness",
                      |            "loanTypes": $closed
                      |        },
                      |        {
                      |            "disposition": "Preapprovals Denied",
                      |            "loanTypes": $preDenied
                      |        },
                      |        {
                      |            "disposition": "Preapprovals Approved but not Accepted",
                      |            "loanTypes": $preAppNotAcc
                      |        },
                      |        {
                      |            "disposition": "Preapprovals Resulting in Originations",
                      |            "loanTypes": $preapproval
                      |        },
                      |        {
                      |            "disposition": "Loans Sold",
                      |            "loanTypes": $sold
                      |        }
                      |    ]
                      |}
       """.stripMargin

      DisclosureReportPayload(metaData.reportTable, "nationwide", report)
    }
  }
}
