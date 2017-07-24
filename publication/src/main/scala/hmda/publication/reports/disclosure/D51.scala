package hmda.publication.reports.disclosure

import java.util.Calendar

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.publication.reports.ApplicantIncomeEnum._
import hmda.publication.reports._
import hmda.model.publication.reports.ReportTypeEnum.Disclosure
import hmda.model.publication.reports._
import hmda.publication.reports.util.DateUtil._
import hmda.publication.reports.util.DispositionTypes._
import hmda.publication.reports.util.ReportUtil._
import hmda.query.model.filing.LoanApplicationRegisterQuery

import scala.concurrent.Future

case class D51(
  respondentId: String,
  institutionName: String,
  table: String,
  reportType: ReportTypeEnum,
  description: String,
  year: Int,
  reportDate: String,
  msa: MSAReport,
  applicantIncomes: List[ApplicantIncome],
  total: List[Disposition]
)

object D51 {
  def apply(
    respondentId: String,
    institutionName: String,
    year: Int,
    reportDate: String,
    msa: MSAReport,
    applicantIncomes: List[ApplicantIncome],
    total: List[Disposition]
  ): D51 = {

    val description = "Disposition of applications for FHA, FSA/RHS, and VA home-purchase loans, 1- to 4-family and manufactured home dwellings, by income, race and ethnicity of applicant"

    D51(
      respondentId,
      institutionName,
      "5-1",
      Disclosure,
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
  // Loan Type 2,3,4
  // Property Type 1,2
  // Purpose of Loan 1
  def generate[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegisterQuery, NotUsed], fipsCode: Int, respId: String): Future[D51] = {

    val lars = larSource
      .filter(lar => lar.respondentId == respId)
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

    val lars50 = larsWithIncome
      .filter(lar => lar.income.toInt < incomeIntervals(0))

    val lars50To79 = larsWithIncome
      .filter(lar => lar.income.toInt >= incomeIntervals(0) && lar.income.toInt < incomeIntervals(1))

    val lars80To99 = larsWithIncome
      .filter(lar => lar.income.toInt >= incomeIntervals(1) && lar.income.toInt < incomeIntervals(2))

    val lars100To120 = larsWithIncome
      .filter(lar => lar.income.toInt >= incomeIntervals(2) && lar.income.toInt < incomeIntervals(3))

    val lars120 = larsWithIncome
      .filter(lar => lar.income.toInt >= incomeIntervals(3))

    val dateF = calculateDate(larSource)
    val totalF = calculateDispositions(lars, dispositions)

    for {
      races50 <- raceBorrowerCharacteristic(lars50, LessThan50PercentOfMSAMedian, dispositions)
      races50to79 <- raceBorrowerCharacteristic(lars50To79, Between50And79PercentOfMSAMedian, dispositions)
      races80to99 <- raceBorrowerCharacteristic(lars80To99, Between80And99PercentOfMSAMedian, dispositions)
      races100to120 <- raceBorrowerCharacteristic(lars100To120, Between100And119PercentOfMSAMedian, dispositions)
      races120 <- raceBorrowerCharacteristic(lars120, GreaterThan120PercentOfMSAMedian, dispositions)
      date <- dateF
      total <- totalF
    } yield {
      val income50 = ApplicantIncome(
        LessThan50PercentOfMSAMedian,
        List(RaceBorrowerCharacteristic(races50))
      )
      val income50To79 = ApplicantIncome(
        Between50And79PercentOfMSAMedian,
        List(RaceBorrowerCharacteristic(races50to79))
      )
      val income80To99 = ApplicantIncome(
        Between80And99PercentOfMSAMedian,
        List(RaceBorrowerCharacteristic(races80to99))
      )
      val income100To120 = ApplicantIncome(
        Between100And119PercentOfMSAMedian,
        List(RaceBorrowerCharacteristic(races100to120))
      )
      val income120 = ApplicantIncome(
        GreaterThan120PercentOfMSAMedian,
        List(RaceBorrowerCharacteristic(races120))
      )

      D51(
        respId,
        "",
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
