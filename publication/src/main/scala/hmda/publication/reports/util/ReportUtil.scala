package hmda.publication.reports.util

import java.util.Calendar

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.census.model._
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.publication.reports.ApplicantIncomeEnum._
import hmda.model.publication.reports._
import hmda.publication.reports.{ AS, EC, MAT }
import hmda.publication.reports.util.DateUtil._
import hmda.util.SourceUtils

import scala.concurrent.Future
import scala.util.Success

object ReportUtil extends SourceUtils {

  def formattedCurrentDate: String = {
    formatDate(Calendar.getInstance().toInstant)
  }

  def msaReport(fipsCode: String): MSAReport = {
    CbsaLookup.values.find(x => x.cbsa == fipsCode || x.metroDiv == fipsCode) match {
      case Some(cbsa) =>
        val stateFips = cbsa.key.substring(0, 2)
        val state = StateAbrvLookup.values.find(s => s.state == stateFips).getOrElse(StateAbrv("", "", ""))
        MSAReport(fipsCode, CbsaLookup.nameFor(fipsCode), state.stateAbrv, state.stateName)
      case None => MSAReport("", "", "", "")
    }
  }

  def calculateMedianIncomeIntervals(fipsCode: Int): Array[Double] = {
    val msaIncome = MsaIncomeLookup.values.find(msa => msa.fips == fipsCode).getOrElse(MsaIncome())
    val medianIncome = msaIncome.income.toDouble / 1000
    val i50 = medianIncome * 0.5
    val i80 = medianIncome * 0.8
    val i100 = medianIncome
    val i120 = medianIncome * 1.2
    Array(i50, i80, i100, i120)
  }

  def larInIncomeInterval(lar: LoanApplicationRegister, applicantIncomeEnum: ApplicantIncomeEnum): Boolean = {
    val incomeIntervals = calculateMedianIncomeIntervals(lar.geography.msa.toInt)
    val income = lar.applicant.income.toInt
    (income < incomeIntervals(0) && applicantIncomeEnum == LessThan50PercentOfMSAMedian) ||
      (income >= incomeIntervals(0) && income < incomeIntervals(1) && applicantIncomeEnum == Between50And79PercentOfMSAMedian) ||
      (income >= incomeIntervals(1) && income < incomeIntervals(2) && applicantIncomeEnum == Between80And99PercentOfMSAMedian) ||
      (income >= incomeIntervals(2) && income < incomeIntervals(3) && applicantIncomeEnum == Between100And119PercentOfMSAMedian) ||
      (income >= incomeIntervals(3) && applicantIncomeEnum == GreaterThan120PercentOfMSAMedian)
  }

  def nationalLarsByIncomeInterval(larSource: Source[LoanApplicationRegister, NotUsed]): Map[ApplicantIncomeEnum, Source[LoanApplicationRegister, NotUsed]] = {
    val lars50 = larSource
      .filter(lar => larInIncomeInterval(lar, LessThan50PercentOfMSAMedian))

    val lars50To79 = larSource
      .filter(lar => larInIncomeInterval(lar, Between50And79PercentOfMSAMedian))

    val lars80To99 = larSource
      .filter(lar => larInIncomeInterval(lar, Between80And99PercentOfMSAMedian))

    val lars100To120 = larSource
      .filter(lar => larInIncomeInterval(lar, Between100And119PercentOfMSAMedian))

    val lars120 = larSource
      .filter(lar => larInIncomeInterval(lar, GreaterThan120PercentOfMSAMedian))

    Map(
      LessThan50PercentOfMSAMedian -> lars50,
      Between50And79PercentOfMSAMedian -> lars50To79,
      Between80And99PercentOfMSAMedian -> lars80To99,
      Between100And119PercentOfMSAMedian -> lars100To120,
      GreaterThan120PercentOfMSAMedian -> lars120
    )
  }

  def larsByIncomeInterval(larSource: Source[LoanApplicationRegister, NotUsed], incomeIntervals: Array[Double]): Map[ApplicantIncomeEnum, Source[LoanApplicationRegister, NotUsed]] = {
    val lars50 = larSource
      .filter(lar => lar.applicant.income.toInt < incomeIntervals(0))

    val lars50To79 = larSource
      .filter(lar => lar.applicant.income.toInt >= incomeIntervals(0) && lar.applicant.income.toInt < incomeIntervals(1))

    val lars80To99 = larSource
      .filter(lar => lar.applicant.income.toInt >= incomeIntervals(1) && lar.applicant.income.toInt < incomeIntervals(2))

    val lars100To120 = larSource
      .filter(lar => lar.applicant.income.toInt >= incomeIntervals(2) && lar.applicant.income.toInt < incomeIntervals(3))

    val lars120 = larSource
      .filter(lar => lar.applicant.income.toInt >= incomeIntervals(3))

    Map(
      LessThan50PercentOfMSAMedian -> lars50,
      Between50And79PercentOfMSAMedian -> lars50To79,
      Between80And99PercentOfMSAMedian -> lars80To99,
      Between100And119PercentOfMSAMedian -> lars100To120,
      GreaterThan120PercentOfMSAMedian -> lars120
    )
  }

  def calculateYear[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegister, NotUsed]): Future[Int] = {
    collectHeadValue(larSource).map {
      case Success(lar) => lar.actionTakenDate.toString.substring(0, 4).toInt
      case _ => 0
    }
  }

  def calculateDispositions[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegister, NotUsed],
    dispositions: List[DispositionType]
  ): Future[List[ValueDisposition]] = {
    Future.sequence(dispositions.map(_.calculateValueDisposition(larSource)))
  }

  def calculatePercentageDispositions[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegister, NotUsed],
    dispositions: List[DispositionType],
    totalDisp: DispositionType
  ): Future[List[PercentageDisposition]] = {

    val calculatedDispositionsF: Future[List[PercentageDisposition]] =
      Future.sequence(dispositions.map(_.calculatePercentageDisposition(larSource)))

    for {
      calculatedDispositions <- calculatedDispositionsF
      totalCount <- count(larSource.filter(totalDisp.filter))
    } yield {

      val withPercentages: List[PercentageDisposition] = calculatedDispositions.map { d =>
        val percentage = if (d.count == 0) 0 else (d.count * 100 / totalCount)
        d.copy(percentage = percentage)
      }
      val totalPercent = if (totalCount == 0) 0 else 100
      val total = PercentageDisposition(totalDisp.value, totalCount, totalPercent)

      withPercentages :+ total
    }
  }

}
