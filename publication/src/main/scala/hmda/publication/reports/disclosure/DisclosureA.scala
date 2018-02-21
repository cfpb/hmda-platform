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

object A1 extends DisclosureReport {
  val reportId = "DA1"
  def filters(lar: LoanApplicationRegister, msa: Int): Boolean = {
    (1 to 4).contains(lar.loan.loanType) &&
      lar.loan.propertyType == 1 &&
      lar.geography.msa.toInt == msa
  }

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
                      |    "msa": $msa,
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
}

object A1W extends DisclosureReport {
  val reportId = "DA1W"
  def filters(lar: LoanApplicationRegister): Boolean = {
    (1 to 4).contains(lar.loan.loanType) &&
      lar.loan.propertyType == 1
  }

  def generate[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegister, NotUsed],
    fipsCode: Int,
    institution: Institution
  ): Future[DisclosureReportPayload] = {

    val metaData = ReportsMetaDataLookup.values(reportId)

    val lars = larSource
      .filter(lar => filters(lar))

    val msa = msaReport(fipsCode.toString).toJsonFormat
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
                      |    "msa": $msa,
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
                      |            "loantypes": $appDenied
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
                      |            "disposition": "Preapprovals Denied",
                      |            "loantypes": $preDenied
                      |        },
                      |        {
                      |            "disposition": "Preapprovals Approved but not Accepted",
                      |            "loantypes": $preAppNotAcc
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
}
