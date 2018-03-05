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

object A1 extends AX {
  val reportId = "DA1"
  def filters(lar: LoanApplicationRegister): Boolean = {
    (1 to 4).contains(lar.loan.loanType) &&
      lar.loan.propertyType == 1
  }
}

object A2 extends AX {
  val reportId = "DA2"
  def filters(lar: LoanApplicationRegister): Boolean = {
    (1 to 4).contains(lar.loan.loanType) &&
      lar.loan.propertyType == 2
  }
}

object A3 extends AX {
  val reportId = "DA3"
  def filters(lar: LoanApplicationRegister): Boolean = {
    (1 to 4).contains(lar.loan.loanType) &&
      lar.loan.propertyType == 3
  }
}

trait AX extends DisclosureReport {
  val reportId: String
  def filters(lar: LoanApplicationRegister): Boolean

  def generate[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegister, NotUsed],
    fipsCode: Int,
    institution: Institution,
    msaList: List[Int]
  ): Future[DisclosureReportPayload] = {

    val metaData = ReportsMetaDataLookup.values(reportId)

    val lars = larSource
      .filter(lar => lar.geography.msa != "NA")
      .filter(lar => lar.geography.msa.toInt == fipsCode)
      .filter(filters)

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
                      |            "loanTypes": $denied
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

      DisclosureReportPayload(metaData.reportTable, fipsCode.toString, report)
    }
  }
}
