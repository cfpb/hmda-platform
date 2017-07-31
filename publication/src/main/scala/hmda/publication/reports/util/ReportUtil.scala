package hmda.publication.reports.util

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.census.model._
import hmda.model.publication.reports._
import hmda.model.publication.reports.RaceEnum._
import hmda.publication.reports.{ AS, EC, MAT }
import hmda.publication.reports.util.DispositionType._
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

  def calculateYear[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegisterQuery, NotUsed]): Future[Int] = {
    collectHeadValue(larSource).map(lar => lar.actionTakenDate.toString.substring(0, 4).toInt)
  }

  def calculateDispositions[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegisterQuery, NotUsed], dispositions: List[DispositionType]): Future[List[Disposition]] = {
    Future.sequence(dispositions.map(_.calculateDisposition(larSource)))
  }

}
