package hmda.publication.reports.disclosure

import java.util.Calendar

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.publication.reports.ApplicantIncomeEnum._
import hmda.publication.reports._
import hmda.model.publication.reports._
import hmda.publication.reports.util.DateUtil._
import hmda.publication.reports.util.ReportUtil._
import hmda.publication.reports.util.ReportsMetaDataLookup
import hmda.query.model.filing.LoanApplicationRegisterQuery

import scala.concurrent.Future

case class D51(
  respondentId: String,
  institutionName: String,
  year: Int,
  reportDate: String,
  msa: MSAReport,
  applicantIncomes: List[ApplicantIncome],
  total: List[Disposition],
  table: String = D51.metaData.reportTable,
  description: String = D51.metaData.description
) extends DisclosureReport

object D51 {
  val metaData = ReportsMetaDataLookup.values("D51")
  val dispositions = metaData.dispositions

  // Table filters:
  // Loan Type 2,3,4
  // Property Type 1,2
  // Purpose of Loan 1
  def generate[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegisterQuery, NotUsed],
    fipsCode: Int,
    respondentId: String,
    institutionNameF: Future[String]
  ): Future[D51] = {

    val lars = larSource
      .filter(lar => lar.respondentId == respondentId)
      .filter(lar => lar.msa != "NA")
      .filter(lar => lar.msa.toInt == fipsCode)
      .filter { lar =>
        (lar.loanType == 2 || lar.loanType == 3 || lar.loanType == 4) &&
          (lar.propertyType == 1 || lar.propertyType == 2) &&
          (lar.purpose == 1)
      }
    val larsWithIncome = lars.filter(lar => lar.income != "NA")

    val msa = msaReport(fipsCode.toString)

    val incomeIntervals = calculateMedianIncomeIntervals(fipsCode)

    val larsByIncome = larsByIncomeInterval(larsWithIncome, incomeIntervals)
    val borrowerCharacteristicsByIncomeF = borrowerCharacteristicsByIncomeInterval(larsByIncome, dispositions)

    val yearF = calculateYear(larSource)
    val totalF = calculateDispositions(lars, dispositions)

    for {
      lars50BorrowerCharacteristics <- borrowerCharacteristicsByIncomeF(LessThan50PercentOfMSAMedian)
      lars50To79BorrowerCharacteristics <- borrowerCharacteristicsByIncomeF(Between50And79PercentOfMSAMedian)
      lars80To99BorrowerCharacteristics <- borrowerCharacteristicsByIncomeF(Between80And99PercentOfMSAMedian)
      lars100To120BorrowerCharacteristics <- borrowerCharacteristicsByIncomeF(Between100And119PercentOfMSAMedian)
      lars120BorrowerCharacteristics <- borrowerCharacteristicsByIncomeF(GreaterThan120PercentOfMSAMedian)

      institutionName <- institutionNameF
      year <- yearF
      total <- totalF
    } yield {
      val income50 = ApplicantIncome(
        LessThan50PercentOfMSAMedian,
        lars50BorrowerCharacteristics
      )
      val income50To79 = ApplicantIncome(
        Between50And79PercentOfMSAMedian,
        lars50To79BorrowerCharacteristics
      )
      val income80To99 = ApplicantIncome(
        Between80And99PercentOfMSAMedian,
        lars80To99BorrowerCharacteristics
      )
      val income100To120 = ApplicantIncome(
        Between100And119PercentOfMSAMedian,
        lars100To120BorrowerCharacteristics
      )
      val income120 = ApplicantIncome(
        GreaterThan120PercentOfMSAMedian,
        lars120BorrowerCharacteristics
      )

      val applicantIncomes = List(
        income50,
        income50To79,
        income80To99,
        income100To120,
        income120
      )

      D51(
        respondentId,
        institutionName,
        year,
        formatDate(Calendar.getInstance().toInstant),
        msa,
        applicantIncomes,
        total
      )
    }

  }
}
