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
import hmda.publication.reports.util.RaceUtil.raceBorrowerCharacteristic
import hmda.publication.reports.util.EthnicityUtil.ethnicityBorrowerCharacteristic
import hmda.publication.reports.util.MinorityStatusUtil.minorityStatusBorrowerCharacteristic
import hmda.util.SourceUtils

import scala.concurrent.Future

object ReportUtil extends SourceUtils {

  def formattedCurrentDate: String = {
    formatDate(Calendar.getInstance().toInstant)
  }

  def msaReport(fipsCode: String): MSAReport = {
    CbsaLookup.values.find(x => x.cbsa == fipsCode) match {
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

  def applicantIncomesWithBorrowerCharacteristics[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegister, NotUsed],
    incomeIntervals: Array[Double],
    dispositions: List[DispositionType]
  ): Future[List[ApplicantIncome]] = {
    val larsByIncome = larsByIncomeInterval(larSource, incomeIntervals)
    val borrowerCharacteristicsByIncomeF = borrowerCharacteristicsByIncomeInterval(larsByIncome, dispositions)

    for {
      lars50BorrowerCharacteristics <- borrowerCharacteristicsByIncomeF(LessThan50PercentOfMSAMedian)
      lars50To79BorrowerCharacteristics <- borrowerCharacteristicsByIncomeF(Between50And79PercentOfMSAMedian)
      lars80To99BorrowerCharacteristics <- borrowerCharacteristicsByIncomeF(Between80And99PercentOfMSAMedian)
      lars100To120BorrowerCharacteristics <- borrowerCharacteristicsByIncomeF(Between100And119PercentOfMSAMedian)
      lars120BorrowerCharacteristics <- borrowerCharacteristicsByIncomeF(GreaterThan120PercentOfMSAMedian)
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

      List(
        income50,
        income50To79,
        income80To99,
        income100To120,
        income120
      )
    }
  }

  def borrowerCharacteristicsByIncomeInterval[ec: EC, mat: MAT, as: AS](
    larsByIncome: Map[ApplicantIncomeEnum, Source[LoanApplicationRegister, NotUsed]],
    dispositions: List[DispositionType]
  ): Map[ApplicantIncomeEnum, Future[List[BorrowerCharacteristic]]] = {
    larsByIncome.map {
      case (income, lars) =>
        val characteristics = Future.sequence(
          List(
            raceBorrowerCharacteristic(lars, dispositions),
            ethnicityBorrowerCharacteristic(lars, dispositions),
            minorityStatusBorrowerCharacteristic(lars, dispositions)
          )
        )
        income -> characteristics
    }
  }

  def calculateYear[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegister, NotUsed]): Future[Int] = {
    collectHeadValue(larSource).map(lar => lar.actionTakenDate.toString.substring(0, 4).toInt)
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
      val total = PercentageDisposition(totalDisp.value, totalCount, 100)

      withPercentages :+ total
    }
  }

}
