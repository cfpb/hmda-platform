package hmda.publication.reports.disclosure

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.census.model.CbsaLookup
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.publication.reports._
import hmda.publication.reports.util.ReportUtil._
import hmda.publication.reports.util.ReportsMetaDataLookup

import scala.concurrent.Future

object DIRS extends DisclosureReport {
  val reportId: String = "DIRS"
  def filters(lar: LoanApplicationRegister): Boolean = true

  def generate[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegister, NotUsed],
    fipsCode: Int,
    institution: Institution,
    msaList: List[Int]
  ): Future[DisclosureReportPayload] = {

    val metaData = ReportsMetaDataLookup.values(reportId)

    val reportDate = formattedCurrentDate
    val yearF = calculateYear(larSource)

    for {
      year <- yearF
      msaList <- createJsonForMSAs(msaList, larSource)
      total <- calculateValuesForMSA("total", larSource)
    } yield {
      val msaJson = msaList.mkString("[", ",", "]")
      val report = s"""
                      |{
                      |    "respondentId": "${institution.respondentId}",
                      |    "institutionName": "${institution.respondent.name}",
                      |    "table": "${metaData.reportTable}",
                      |    "type": "Disclosure",
                      |    "description": "${metaData.description}",
                      |    "year": "$year",
                      |    "reportDate": "$reportDate",
                      |    "msas": $msaJson,
                      |    "total": $total
                      |}
       """.stripMargin

      DisclosureReportPayload(metaData.reportTable, "nationwide", report)
    }
  }

  private def createJsonForMSAs[ec: EC, mat: MAT, as: AS](msaList: List[Int], larSource: Source[LoanApplicationRegister, NotUsed]): Future[Seq[String]] = {
    val list = msaList.map(_.toString) :+ "NA"

    Future.sequence(list.map(msa => calculateValuesForMSA(msa, larSource)))
  }

  private def calculateValuesForMSA[ec: EC, mat: MAT, as: AS](msa: String, larSource: Source[LoanApplicationRegister, NotUsed]): Future[String] = {
    val lars = if (msa == "total") larSource else larSource.filter(_.geography.msa == msa)

    for {
      total <- count(lars)
      amount <- sum(lars, (lar: LoanApplicationRegister) => lar.loan.amount)

      conv <- count(lars.filter(_.loan.loanType == 1))
      fha <- count(lars.filter(_.loan.loanType == 2))
      va <- count(lars.filter(_.loan.loanType == 3))
      fsa <- count(lars.filter(_.loan.loanType == 4))

      oneToFour <- count(lars.filter(_.loan.propertyType == 1))
      manufactured <- count(lars.filter(_.loan.propertyType == 2))
      multifamily <- count(lars.filter(_.loan.propertyType == 3))

      homePurchase <- count(lars.filter(_.loan.purpose == 1))
      homeImprovement <- count(lars.filter(_.loan.purpose == 2))
      refinance <- count(lars.filter(_.loan.purpose == 3))
    } yield {
      val msaId = if (msa == "NA") "-----" else if (msa == "total") "" else msa
      val msaName = if (msa == "NA") "MSA/MD NOT AVAILABLE" else if (msa == "total") "TOTAL" else CbsaLookup.nameFor(msa)
      s"""
         |{
         |  "msa": "$msaId",
         |  "msaName": "$msaName",
         |  "totalLars": $total,
         |  "totalAmount": $amount,
         |  "conv": $conv,
         |  "fha": $fha,
         |  "va": $va,
         |  "fsaRhs": $fsa,
         |  "oneToFourFamily": $oneToFour,
         |  "manufHome": $manufactured,
         |  "multiFamily": $multifamily,
         |  "homePurchase": $homePurchase,
         |  "homeImprovement": $homeImprovement,
         |  "refinance": $refinance
         |}
       """.stripMargin
    }
  }
}
