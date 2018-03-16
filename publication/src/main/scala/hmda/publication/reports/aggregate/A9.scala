package hmda.publication.reports.aggregate
import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.census.model.{ Tract, TractLookup }
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.publication.reports.ValueDisposition
import hmda.publication.reports.util.CensusTractUtil._
import hmda.publication.reports.util.DispositionType._
import hmda.publication.reports.util.ReportsMetaDataLookup
import hmda.publication.reports.util.ReportUtil.{ calculateYear, formattedCurrentDate, msaReport }
import hmda.publication.reports.{ AS, EC, MAT }

import scala.concurrent.Future

object A9 extends Aggregate9 {
  val reportId: String = "A9"
  def fipsString(fips: Int): String = fips.toString
  def msaString(fips: Int): String = s""""msa": ${msaReport(fips.toString).toJsonFormat},"""
  def msaTracts(fips: Int): Set[Tract] = TractLookup.values.filter(_.msa == fips.toString)
  def msaLars(larSource: Source[LoanApplicationRegister, NotUsed], fips: Int): Source[LoanApplicationRegister, NotUsed] =
    larSource.filter(_.geography.msa == fips)
}
object N9 extends Aggregate9 {
  val reportId: String = "N9"
  def fipsString(fips: Int): String = "nationwide"
  def msaString(fips: Int): String = ""
  def msaTracts(fips: Int): Set[Tract] = TractLookup.values
  def msaLars(larSource: Source[LoanApplicationRegister, NotUsed], fips: Int): Source[LoanApplicationRegister, NotUsed] =
    larSource.filter(_.geography.msa != "NA")
}

trait Aggregate9 extends AggregateReport {
  val reportId: String
  def fipsString(fips: Int): String
  def msaString(fips: Int): String
  def msaTracts(fips: Int): Set[Tract]
  def msaLars(larSource: Source[LoanApplicationRegister, NotUsed], fips: Int): Source[LoanApplicationRegister, NotUsed]

  val loanCategories = List(FHA, Conventional, Refinancings, HomeImprovementLoans,
    LoansForFiveOrMore, NonoccupantLoans, ManufacturedHomeDwellings)
  val dispositions = List(LoansOriginated, ApprovedButNotAccepted,
    ApplicationsDenied, ApplicationsWithdrawn, ClosedForIncompleteness)

  def filters(lar: LoanApplicationRegister): Boolean = (1 to 5).contains(lar.actionTakenType)

  override def generate[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegister, NotUsed],
    fipsCode: Int
  ): Future[AggregateReportPayload] = {

    val metaData = ReportsMetaDataLookup.values(reportId)

    val reportLars = larSource.filter(filters)

    val yearF = calculateYear(reportLars)
    val reportDate = formattedCurrentDate
    val lars = msaLars(reportLars, fipsCode)
    val tracts = msaTracts(fipsCode)
    val larsUnknownMedianAgeInTract = filterUnknownMedianYearBuilt(reportLars, tracts)

    for {
      year <- yearF

      a1 <- loanCategoryDispositions(filterMedianYearHomesBuilt(lars, 2000, 2010, tracts))
      a2 <- loanCategoryDispositions(filterMedianYearHomesBuilt(lars, 1990, 2000, tracts))
      a3 <- loanCategoryDispositions(filterMedianYearHomesBuilt(lars, 1980, 1990, tracts))
      a4 <- loanCategoryDispositions(filterMedianYearHomesBuilt(lars, 1970, 1980, tracts))
      a5 <- loanCategoryDispositions(filterMedianYearHomesBuilt(lars, 0, 1980, tracts))
      a6 <- loanCategoryDispositions(larsUnknownMedianAgeInTract)

    } yield {
      val report =
        s"""
           |{
           |    "table": "${metaData.reportTable}",
           |    "type": "${metaData.reportType}",
           |    "description": "${metaData.description}",
           |    "year": "$year",
           |    "reportDate": "$reportDate",
           |    ${msaString(fipsCode)}
           |    "characteristic": "Census Tracts by Median Age of Homes",
           |    "medianAges": [
           |        {
           |            "medianAge": "2000 - 2010",
           |            "loanCategories": $a1
           |        },
           |        {
           |            "medianAge": "1990 - 1999",
           |            "loanCategories": $a2
           |        },
           |        {
           |            "medianAge": "1980 - 1989",
           |            "loanCategories": $a3
           |        },
           |        {
           |            "medianAge": "1970 - 1979",
           |            "loanCategories": $a4
           |        },
           |        {
           |            "medianAge": "1969 or Earlier",
           |            "loanCategories": $a5
           |        },
           |        {
           |            "medianAge": "Age Unknown",
           |            "loanCategories": $a6
           |        }
           |    ]
           |}
         """.stripMargin
      AggregateReportPayload(reportId, fipsString(fipsCode), report)
    }

  }

  private def loanCategoryDispositions[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegister, NotUsed]): Future[String] = {
    val loanCategoryOutputs: Future[List[String]] = Future.sequence(
      loanCategories.map { loanCategory =>
        dispositionsOutput(larSource.filter(loanCategory.filter)).map { disp =>
          s"""
             |{
             |    "loanCategory": "${loanCategory.value}",
             |    "dispositions": $disp
             |}
           """.stripMargin
        }
      }
    )
    loanCategoryOutputs.map { list => list.mkString("[", ",", "]") }
  }

  private def dispositionsOutput[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegister, NotUsed]): Future[String] = {
    val calculatedDispositions: Future[List[ValueDisposition]] = Future.sequence(
      dispositions.map(_.calculateValueDisposition(larSource))
    )

    calculatedDispositions.map(list => list.map(_.toJsonFormat).mkString("[", ",", "]"))
  }
}
