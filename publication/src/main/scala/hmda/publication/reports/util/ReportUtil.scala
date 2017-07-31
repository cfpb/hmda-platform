package hmda.publication.reports.util

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.census.model._
import hmda.model.publication.reports.ApplicantIncomeEnum._
import hmda.model.publication.reports._
import hmda.publication.reports.{ AS, EC, MAT }
import hmda.publication.reports.util.DispositionType._
import hmda.publication.reports.util.RaceUtil.raceBorrowerCharacteristic
import hmda.publication.reports.util.EthnicityUtil.ethnicityBorrowerCharacteristic
import hmda.publication.reports.util.MinorityStatusUtil.minorityStatusBorrowerCharacteristic
import hmda.query.model.filing.LoanApplicationRegisterQuery
import hmda.util.SourceUtils

import scala.concurrent.Future

object ReportUtil extends SourceUtils {

  def msaReport(fipsCode: String): MSAReport = {
    val cbsa = CbsaLookup.values.find(x => x.cbsa == fipsCode).getOrElse(Cbsa())
    val stateFips = cbsa.key.substring(0, 2)
    val state = StateAbrvLookup.values.find(s => s.state == stateFips).getOrElse(StateAbrv("", "", ""))
    MSAReport(fipsCode, CbsaLookup.nameFor(fipsCode), state.stateAbrv, state.stateName)
  }

  def calculateMedianIncomeIntervals(fipsCode: Int): Array[Double] = {
    val msaIncome = MsaIncomeLookup.values.find(msa => msa.fips == fipsCode).getOrElse(MsaIncome())
    val medianIncome = msaIncome.income / 1000
    val i50 = medianIncome * 0.5
    val i80 = medianIncome * 0.8
    val i100 = medianIncome
    val i120 = medianIncome * 1.2
    Array(i50, i80, i100, i120)
  }

  def larsByIncomeInterval(larSource: Source[LoanApplicationRegisterQuery, NotUsed], incomeIntervals: Array[Double]): Map[ApplicantIncomeEnum, Source[LoanApplicationRegisterQuery, NotUsed]] = {
    val lars50 = larSource
      .filter(lar => lar.income.toInt < incomeIntervals(0))

    val lars50To79 = larSource
      .filter(lar => lar.income.toInt >= incomeIntervals(0) && lar.income.toInt < incomeIntervals(1))

    val lars80To99 = larSource
      .filter(lar => lar.income.toInt >= incomeIntervals(1) && lar.income.toInt < incomeIntervals(2))

    val lars100To120 = larSource
      .filter(lar => lar.income.toInt >= incomeIntervals(2) && lar.income.toInt < incomeIntervals(3))

    val lars120 = larSource
      .filter(lar => lar.income.toInt >= incomeIntervals(3))

    Map(
      LessThan50PercentOfMSAMedian -> lars50,
      Between50And79PercentOfMSAMedian -> lars50To79,
      Between80And99PercentOfMSAMedian -> lars80To99,
      Between100And119PercentOfMSAMedian -> lars100To120,
      GreaterThan120PercentOfMSAMedian -> lars120
    )
  }

  def borrowerCharacteristicsByIncomeInterval[ec: EC, mat: MAT, as: AS](larsByIncome: Map[ApplicantIncomeEnum, Source[LoanApplicationRegisterQuery, NotUsed]], dispositions: List[DispositionType]): Map[ApplicantIncomeEnum, Future[List[BorrowerCharacteristic]]] = {
    larsByIncome.map {
      case (income, lars) =>
        val characteristics = Future.sequence(
          List(
            raceBorrowerCharacteristic(lars, income, dispositions),
            ethnicityBorrowerCharacteristic(lars, income, dispositions),
            minorityStatusBorrowerCharacteristic(lars, income, dispositions)
          )
        )
        income -> characteristics
    }
  }

  def calculateYear[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegisterQuery, NotUsed]): Future[Int] = {
    collectHeadValue(larSource).map(lar => lar.actionTakenDate.toString.substring(0, 4).toInt)
  }

  def calculateDispositions[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegisterQuery, NotUsed], dispositions: List[DispositionType]): Future[List[Disposition]] = {
    Future.sequence(dispositions.map(_.calculateDisposition(larSource)))
  }

}
