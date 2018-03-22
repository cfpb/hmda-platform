package hmda.publication.reports.aggregate

import akka.NotUsed
import akka.stream.scaladsl._
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.publication.reports.util.ReportUtil._
import hmda.publication.reports.util.ReportsMetaDataLookup
import hmda.publication.reports.{ AS, EC, MAT }

import scala.concurrent.Future

object AI extends AggregateReport {
  val reportId: String = "AI"
  def filters(lar: LoanApplicationRegister): Boolean = true

  def generateWithInst[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegister, NotUsed],
    fipsCode: Int,
    inst: Future[Set[Institution]]
  ): Future[AggregateReportPayload] = {
    val metaData = ReportsMetaDataLookup.values(reportId)

    val lars = larSource
      .filter(lar => lar.geography.msa != "NA")
      .filter(lar => lar.geography.msa.toInt == fipsCode)

    val msa: String = s""""msa": ${msaReport(fipsCode.toString).toJsonFormat},"""
    val reportDate = formattedCurrentDate
    val yearF = calculateYear(lars)

    for {
      year <- yearF
      i <- inst
      names <- larsToInst(lars, i)
    } yield {
      val namesStr = names.mkString("[", ",", "]")
      val report = s"""
                      |{
                      |    "table": "${metaData.reportTable}",
                      |    "type": "${metaData.reportType}",
                      |    "description": "${metaData.description}",
                      |    "year": "$year",
                      |    "reportDate": "$reportDate",
                      |    $msa
                      |    "institutions": $namesStr
                      |}
                      |
       """.stripMargin

      AggregateReportPayload(reportId, fipsCode.toString, report)
    }
  }

  // Not used, placeholder
  def generate[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegister, NotUsed],
    fipsCode: Int
  ): Future[AggregateReportPayload] = {
    Future(AggregateReportPayload(reportId, fipsCode.toString, ""))
  }

  private def larsToInst[ec: EC, mat: MAT, as: AS](lars: Source[LoanApplicationRegister, NotUsed], inst: Set[Institution]): Future[Set[String]] = {
    val idsF = lars.map(_.respondentId).runWith(Sink.seq)
    for {
      ids <- idsF
    } yield {
      inst.filter(i => ids.contains(i.respondentId)).map(i => s""""${i.respondent.name}"""")
    }
  }

}
