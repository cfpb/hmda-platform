package hmda.publication.reports.disclosure

import akka.NotUsed
import akka.stream.scaladsl.{ Sink, Source }
import hmda.census.model.{ Cbsa, CbsaLookup, Tract }
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.publication.reports._
import hmda.publication.reports.protocol.DispositionProtocol
import hmda.publication.reports.util.DispositionType._
import hmda.publication.reports.util.ReportUtil._
import hmda.publication.reports.util.{ DispositionType, ReportsMetaDataLookup }
import spray.json._

import scala.concurrent.Future

object D2 extends DisclosureReport with DispositionProtocol {
  val reportId: String = "D2"

  def generate[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegister, NotUsed],
    fipsCode: Int,
    institution: Institution
  ): Future[DisclosureReportPayload] = {
    val metaData = ReportsMetaDataLookup.values(reportId)

    val yearF = calculateYear(larSource)
    val reportDate = formattedCurrentDate
    val msa = msaReport(fipsCode.toString).toJsonFormat

    val dispositions = List(FHA, Conventional, Refinancings,
      HomeImprovementLoans, LoansForFiveOrMore, NonoccupantLoans, ManufacturedHomeDwellings)

    val lars = larSource
      .filter(lar => lar.geography.msa == fipsCode.toString)
      .filter(lar => lar.actionTakenType == 6)

    for {
      year <- yearF
      t <- getTracts(lars)
      s <- calculateD2TractValues(t, lars, dispositions)
    } yield {
      val tractJson = s.mkString("[", ",", "]")
      val report =
        s"""
           |{
           |    "respondentId": "${institution.respondentId}",
           |    "institutionName": "${institution.respondent.name}",
           |    "table": "${metaData.reportTable}",
           |    "type": "Disclosure",
           |    "description": "${metaData.description}",
           |    "year": "$year",
           |    "reportDate": "$reportDate",
           |    "msa": $msa,
           |    "tracts": $tractJson
           |}
         """.stripMargin

      DisclosureReportPayload(metaData.reportTable, fipsCode.toString, report)
    }
  }

  private def getTracts[ec: EC, mat: MAT, as: AS](source: Source[LoanApplicationRegister, NotUsed]): Future[Set[Tract]] = {
    val fTracts = source.map(lar => Tract(lar.geography.state, lar.geography.county, lar.geography.tract)).runWith(Sink.seq)
    for {
      t <- fTracts
    } yield t.toSet
  }

  private def getTractTitle(tract: Tract): String = {
    val cbsa = CbsaLookup.values.find(cbsa => cbsa.key == tract.state + tract.county).getOrElse(Cbsa())
    val stateAbbr = cbsa.cbsaTitle.split(",")(1).trim
    s"$stateAbbr/${cbsa.countyName}/${tract.tract}"
  }

  private def calculateD2TractValues[ec: EC, mat: MAT, as: AS](tracts: Set[Tract], lars: Source[LoanApplicationRegister, NotUsed], dispositions: List[DispositionType]): Future[Set[String]] = {
    Future.sequence(tracts.map(tract => {
      val tractLars = lars.filter(lar =>
        lar.geography.state == tract.state &&
          lar.geography.county == tract.county &&
          lar.geography.tract == tract.tract)

      for {
        a1 <- calculateDispositions(tractLars, dispositions)
      } yield {
        val tractTitle = getTractTitle(tract)
        s"""
           |{
           |  "tract": "$tractTitle",
           |  "values": ${a1.toJson}
           |}
           """.stripMargin
      }
    }))
  }
}
