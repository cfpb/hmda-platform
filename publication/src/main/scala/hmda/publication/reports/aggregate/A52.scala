package hmda.publication.reports.aggregate

import java.util.Calendar

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.publication.reports.ApplicantIncomeEnum._
import hmda.model.publication.reports.{ ApplicantIncome, Disposition, MSAReport }
import hmda.publication.reports._
import hmda.publication.reports.util.DateUtil._
import hmda.publication.reports.util.DispositionType.{ ClosedDisp, WithdrawnDisp, _ }
import hmda.publication.reports.util.ReportUtil._
import hmda.query.model.filing.LoanApplicationRegisterQuery

import scala.concurrent.Future

case class A52(
  table: String,
  description: String,
  year: Int,
  reportDate: String,
  msa: MSAReport,
  applicantIncomes: List[ApplicantIncome],
  total: List[Disposition]
) extends AggregateReport

object A52 {
  def apply(
    year: Int,
    reportDate: String,
    msa: MSAReport,
    applicantIncomes: List[ApplicantIncome],
    total: List[Disposition]
  ): A52 = {

    val description = "Disposition of Applications for Conventional Home-Purchase Loans, 1-to-4 Family and Manufactured Home Dwellings, by Income, Race, and Ethnicity of Applicant"

    A52(
      "5-2",
      description,
      year,
      reportDate,
      msa,
      applicantIncomes,
      total
    )
  }

  val dispositions: List[DispositionType] =
    List(
      ReceivedDisp,
      OriginatedDisp,
      ApprovedButNotAcceptedDisp,
      DeniedDisp,
      WithdrawnDisp,
      ClosedDisp
    )

  // Table filters:
  // Loan Type 1
  // Property Type 1,2
  // Purpose of Loan 1
  def generate[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegisterQuery, NotUsed],
    fipsCode: Int
  ): Future[A52] = {
    val lars = larSource
      .filter(lar => lar.msa != "NA")
      .filter(lar => lar.msa.toInt == fipsCode)
      .filter { lar =>
        (lar.loanType == 1) &&
          (lar.propertyType == 1 || lar.propertyType == 2) &&
          (lar.purpose == 1)
      }

    val larsWithIncome = lars.filter(lar => lar.income != "NA")

    val msa = msaReport(fipsCode.toString)

    val incomeIntervals = calculateMedianIncomeIntervals(fipsCode)

    val larsByIncome = larsByIncomeInterval(larsWithIncome, incomeIntervals)
    val borrowerCharacteristicsByIncomeF = borrowerCharacteristicsByIncomeInterval(larsByIncome, dispositions)

    val dateF = calculateYear(larSource)
    val totalF = calculateDispositions(lars, dispositions)

    for {
      lars50BorrowerCharacteristics <- borrowerCharacteristicsByIncomeF(LessThan50PercentOfMSAMedian)
      lars50To79BorrowerCharacteristics <- borrowerCharacteristicsByIncomeF(Between50And79PercentOfMSAMedian)
      lars80To99BorrowerCharacteristics <- borrowerCharacteristicsByIncomeF(Between80And99PercentOfMSAMedian)
      lars100To120BorrowerCharacteristics <- borrowerCharacteristicsByIncomeF(Between100And119PercentOfMSAMedian)
      lars120BorrowerCharacteristics <- borrowerCharacteristicsByIncomeF(GreaterThan120PercentOfMSAMedian)

      date <- dateF
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

      A52(
        date,
        formatDate(Calendar.getInstance().toInstant),
        msa,
        List(
          income50,
          income50To79,
          income80To99,
          income100To120,
          income120
        ),
        total
      )
    }
  }
}
