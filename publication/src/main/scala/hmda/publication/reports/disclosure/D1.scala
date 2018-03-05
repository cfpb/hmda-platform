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

import scala.concurrent.Future
import spray.json._

object D1 extends DisclosureReport with DispositionProtocol {
  val reportId: String = "D1"

  def generate[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegister, NotUsed],
    fipsCode: Int,
    institution: Institution,
    msaList: List[Int]
  ): Future[DisclosureReportPayload] = {
    val metaData = ReportsMetaDataLookup.values(reportId)

    val yearF = calculateYear(larSource)
    val reportDate = formattedCurrentDate
    val msa = msaReport(fipsCode.toString).toJsonFormat

    val dispositions = List(FHA, Conventional, Refinancings,
      HomeImprovementLoans, LoansForFiveOrMore, NonoccupantLoans, ManufacturedHomeDwellings)

    val lars = larSource
      .filter(lar => lar.geography.msa == fipsCode.toString)
      .filter(lar => (1 to 5).contains(lar.actionTakenType))

    for {
      year <- yearF
      t <- getTracts(lars)
      s <- calculateD1TractValues(t, lars, dispositions)
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

  private def calculateD1TractValues[ec: EC, mat: MAT, as: AS](tracts: Set[Tract], lars: Source[LoanApplicationRegister, NotUsed], dispositions: List[DispositionType]): Future[Set[String]] = {
    Future.sequence(tracts.map(tract => {
      val tractLars = lars.filter(lar =>
        lar.geography.state == tract.state &&
          lar.geography.county == tract.county &&
          lar.geography.tract == tract.tract)

      for {
        a1 <- calculateDispositions(tractLars.filter(_.actionTakenType == 1), dispositions)
        a2 <- calculateDispositions(tractLars.filter(_.actionTakenType == 2), dispositions)
        a3 <- calculateDispositions(tractLars.filter(_.actionTakenType == 3), dispositions)
        a4 <- calculateDispositions(tractLars.filter(_.actionTakenType == 4), dispositions)
        a5 <- calculateDispositions(tractLars.filter(_.actionTakenType == 5), dispositions)
      } yield {
        val tractTitle = getTractTitle(tract)
        s"""
           |{
           |  "tract": "$tractTitle",
           |  "dispositions": [
           |    {
           |      "title": "Loans originated",
           |      "values": ${a1.toJson}
           |    },
           |    {
           |      "title": "Apps approved, not accepted",
           |      "values": ${a2.toJson}
           |    },
           |    {
           |      "title": "Apps denied",
           |      "values": ${a3.toJson}
           |    },
           |    {
           |      "title": "Apps withdrawn",
           |      "values": ${a4.toJson}
           |    },
           |    {
           |      "title": "Files closed for incompleteness",
           |      "values": ${a5.toJson}
           |    }
           |  ]
           |}
           """.stripMargin
      }
    }))
  }
}
