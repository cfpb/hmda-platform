package hmda.publication.reports.aggregate

import akka.NotUsed
import akka.stream.scaladsl.{ Sink, Source }
import hmda.census.model.{ Cbsa, CbsaLookup, Tract, TractLookup }
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.publication.reports._
import hmda.publication.reports.protocol.DispositionProtocol
import hmda.publication.reports.util.DispositionType._
import hmda.publication.reports.util.ReportUtil._
import hmda.publication.reports.util.{ DispositionType, ReportsMetaDataLookup }
import spray.json._

import scala.concurrent.Future

object A1 extends AggregateReport with DispositionProtocol {
  val reportId: String = "A1"

  def generate[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegister, NotUsed],
    fipsCode: Int
  ): Future[AggregateReportPayload] = {
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
      s <- calculateA1TractValues(t, lars, dispositions)
    } yield {
      val tractJson = s.mkString("[", ",", "]")
      val report =
        s"""
           |{
           |    "table": "${metaData.reportTable}",
           |    "type": "Disclosure",
           |    "description": "${metaData.description}",
           |    "year": "$year",
           |    "reportDate": "$reportDate",
           |    "msa": $msa,
           |    "tracts": $tractJson
           |}
         """.stripMargin

      AggregateReportPayload(metaData.reportTable, fipsCode.toString, report)
    }
  }

  private def getTracts[ec: EC, mat: MAT, as: AS](source: Source[LoanApplicationRegister, NotUsed]): Future[Set[Tract]] = {
    val fTracts = source.map(lar => {
      TractLookup.values
        .find(t => lar.geography.state == t.state && lar.geography.county == t.county && lar.geography.tract == t.tractDec)
        .getOrElse(Tract())
    }).runWith(Sink.seq)
    for {
      t <- fTracts
    } yield t.toSet
  }

  private def getTractTitle(tract: Tract): String = {
    val cbsa = CbsaLookup.values.find(cbsa => cbsa.key == tract.state + tract.county).getOrElse(Cbsa())
    val stateAbbr = cbsa.cbsaTitle.split(",")(1).trim
    s"$stateAbbr/${cbsa.countyName}/${tract.tractDec}"
  }

  private def calculateA1TractValues[ec: EC, mat: MAT, as: AS](tracts: Set[Tract], lars: Source[LoanApplicationRegister, NotUsed], dispositions: List[DispositionType]): Future[Set[String]] = {
    Future.sequence(tracts.map(tract => {
      val tractLars = lars.filter(lar =>
        lar.geography.state == tract.state &&
          lar.geography.county == tract.county &&
          lar.geography.tract == tract.tractDec)

      for {
        a1 <- calculateDispositions(tractLars.filter(_.actionTakenType == 1), dispositions)
        a2 <- calculateDispositions(tractLars.filter(_.actionTakenType == 2), dispositions)
        a3 <- calculateDispositions(tractLars.filter(_.actionTakenType == 3), dispositions)
        a4 <- calculateDispositions(tractLars.filter(_.actionTakenType == 4), dispositions)
        a5 <- calculateDispositions(tractLars.filter(_.actionTakenType == 5), dispositions)
      } yield {
        val tractTitle = getTractTitle(tract)
        val minPopRounded = Math.round(tract.minorityPopulationPercent).toInt
        val medianIncomeRounded = Math.round(tract.tractMfiPercentageOfMsaMfi).toInt
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
           |    },
           |    {
           |      "title": "% Minority Population",
           |      "values": $minPopRounded
           |    },
           |    {
           |      "title": "Median Income as PCT of MSA/MD Median",
           |      "values": $medianIncomeRounded
           |    }
           |  ]
           |}
           """.stripMargin
      }
    }))
  }
}
