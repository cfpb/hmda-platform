package hmda.publication.reports.disclosure

import java.util.Calendar

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.publication.reports.ApplicantIncomeEnum._
import hmda.publication.reports._
import hmda.model.publication.reports.ReportTypeEnum.Disclosure
import hmda.model.publication.reports._
import hmda.publication.reports.util.DateUtil._
import hmda.publication.reports.util.DispositionType._
import hmda.publication.reports.util.EthnicityUtil._
import hmda.publication.reports.util.MinorityStatusUtil._
import hmda.publication.reports.util.RaceUtil._
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

    val larsByIncome = larsByIncomeInterval(larsWithIncome, incomeIntervals)

    val dateF = calculateYear(larSource)
    val totalF = calculateDispositions(lars, dispositions)

    for {
      races50 <- raceBorrowerCharacteristic(larsByIncome(LessThan50PercentOfMSAMedian), LessThan50PercentOfMSAMedian, dispositions)
      races50to79 <- raceBorrowerCharacteristic(larsByIncome(Between50And79PercentOfMSAMedian), Between50And79PercentOfMSAMedian, dispositions)
      races80to99 <- raceBorrowerCharacteristic(larsByIncome(Between80And99PercentOfMSAMedian), Between80And99PercentOfMSAMedian, dispositions)
      races100to120 <- raceBorrowerCharacteristic(larsByIncome(Between100And119PercentOfMSAMedian), Between100And119PercentOfMSAMedian, dispositions)
      races120 <- raceBorrowerCharacteristic(larsByIncome(GreaterThan120PercentOfMSAMedian), GreaterThan120PercentOfMSAMedian, dispositions)

      ethnicity50 <- ethnicityBorrowerCharacteristic(larsByIncome(LessThan50PercentOfMSAMedian), LessThan50PercentOfMSAMedian, dispositions)
      ethnicity50to79 <- ethnicityBorrowerCharacteristic(larsByIncome(Between50And79PercentOfMSAMedian), Between50And79PercentOfMSAMedian, dispositions)
      ethnicity80to99 <- ethnicityBorrowerCharacteristic(larsByIncome(Between80And99PercentOfMSAMedian), Between80And99PercentOfMSAMedian, dispositions)
      ethnicity100to120 <- ethnicityBorrowerCharacteristic(larsByIncome(Between100And119PercentOfMSAMedian), Between100And119PercentOfMSAMedian, dispositions)
      ethnicity120 <- ethnicityBorrowerCharacteristic(larsByIncome(GreaterThan120PercentOfMSAMedian), GreaterThan120PercentOfMSAMedian, dispositions)

      minorityStatus50 <- minorityStatusBorrowerCharacteristic(larsByIncome(LessThan50PercentOfMSAMedian), LessThan50PercentOfMSAMedian, dispositions)
      minorityStatus50to79 <- minorityStatusBorrowerCharacteristic(larsByIncome(Between50And79PercentOfMSAMedian), Between50And79PercentOfMSAMedian, dispositions)
      minorityStatus80to99 <- minorityStatusBorrowerCharacteristic(larsByIncome(Between80And99PercentOfMSAMedian), Between80And99PercentOfMSAMedian, dispositions)
      minorityStatus100to120 <- minorityStatusBorrowerCharacteristic(larsByIncome(Between100And119PercentOfMSAMedian), Between100And119PercentOfMSAMedian, dispositions)
      minorityStatus120 <- minorityStatusBorrowerCharacteristic(larsByIncome(GreaterThan120PercentOfMSAMedian), GreaterThan120PercentOfMSAMedian, dispositions)

      date <- dateF
      total <- totalF
    } yield {
      val income50 = ApplicantIncome(
        LessThan50PercentOfMSAMedian,
        List(
          RaceBorrowerCharacteristic(races50),
          EthnicityBorrowerCharacteristic(ethnicity50),
          MinorityStatusBorrowerCharacteristic(minorityStatus50)
        )
      )
      val income50To79 = ApplicantIncome(
        Between50And79PercentOfMSAMedian,
        List(
          RaceBorrowerCharacteristic(races50to79),
          EthnicityBorrowerCharacteristic(ethnicity50to79),
          MinorityStatusBorrowerCharacteristic(minorityStatus50to79)
        )
      )
      val income80To99 = ApplicantIncome(
        Between80And99PercentOfMSAMedian,
        List(
          RaceBorrowerCharacteristic(races80to99),
          EthnicityBorrowerCharacteristic(ethnicity80to99),
          MinorityStatusBorrowerCharacteristic(minorityStatus80to99)
        )
      )
      val income100To120 = ApplicantIncome(
        Between100And119PercentOfMSAMedian,
        List(
          RaceBorrowerCharacteristic(races100to120),
          EthnicityBorrowerCharacteristic(ethnicity100to120),
          MinorityStatusBorrowerCharacteristic(minorityStatus100to120)
        )
      )
      val income120 = ApplicantIncome(
        GreaterThan120PercentOfMSAMedian,
        List(
          RaceBorrowerCharacteristic(races120),
          EthnicityBorrowerCharacteristic(ethnicity120),
          MinorityStatusBorrowerCharacteristic(minorityStatus120)
        )
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
