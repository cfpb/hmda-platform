package hmda.publication.reports.aggregate

import java.util.Calendar

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.publication.reports.ApplicantIncomeEnum._
import hmda.model.publication.reports.{ ApplicantIncome, Disposition, MSAReport }
import hmda.publication.reports._
import hmda.publication.reports.national.NationalAggregateReport
import hmda.publication.reports.util.DateUtil._
import hmda.publication.reports.util.ReportUtil._
import hmda.publication.reports.util.ReportsMetaDataLookup
import hmda.query.model.filing.LoanApplicationRegisterQuery

import scala.concurrent.Future

case class N52(
                year: Int,
                reportDate: String,
                applicantIncomes: List[ApplicantIncome],
                total: List[Disposition],
                table: String = N52.metaData.reportTable,
                description: String = N52.metaData.description
              ) extends NationalAggregateReport

object N52 {
  val metaData = ReportsMetaDataLookup.values("N52")
  val dispositions = metaData.dispositions

  // Table filters:
  // Loan Type 1
  // Property Type 1,2
  // Purpose of Loan 1
  def generate[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegisterQuery, NotUsed]): Future[N52] = {
    val lars = larSource
      .filter { lar =>
        (lar.loanType == 1) &&
          (lar.propertyType == 1 || lar.propertyType == 2) &&
          (lar.purpose == 1)
      }

    val larsWithIncome = lars.filter(lar => lar.income != "NA")
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

      N52(
        year,
        formatDate(Calendar.getInstance().toInstant),
        applicantIncomes,
        total
      )
    }
  }
}
