package hmda.publication.reports.national

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.census.model.MsaIncomeLookup
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.publication.reports._
import hmda.publication.reports._
import hmda.publication.reports.aggregate.{ A5X, A5XReportCreator }

import scala.concurrent.Future

case class N5X(
    year: Int,
    applicantIncomes: List[ApplicantIncome],
    total: List[Disposition],
    table: String,
    description: String,
    reportDate: String
) extends NationalAggregateReport {

  def +(a5X: A5X): N5X = {
    if (table != a5X.table) throw new IllegalArgumentException

    val incomes: List[ApplicantIncome] =
      if (applicantIncomes.isEmpty) a5X.applicantIncomes else combinedIncomes(a5X.applicantIncomes)

    val totals: List[Disposition] =
      if (applicantIncomes.isEmpty) a5X.total else {

        a5X.total.map(disposition => {
          val originalDisposition = total.find(d => d.disposition == disposition.disposition).get
          disposition + originalDisposition
        })
      }

    N5X(a5X.year, incomes, totals, a5X.table, a5X.description, a5X.reportDate)
  }

  private def combinedIncomes(newIncomes: List[ApplicantIncome]): List[ApplicantIncome] = {
    newIncomes.map(income => {
      val newC = applicantIncomes.find(i => i.applicantIncome == income.applicantIncome).get.borrowerCharacteristics
      val originalC = income.borrowerCharacteristics

      val nR = newC.find(_.isInstanceOf[RaceBorrowerCharacteristic]).get.asInstanceOf[RaceBorrowerCharacteristic]
      val oR = originalC.find(_.isInstanceOf[RaceBorrowerCharacteristic]).get.asInstanceOf[RaceBorrowerCharacteristic]
      val cR = oR + nR

      val nE = newC.find(_.isInstanceOf[EthnicityBorrowerCharacteristic]).get.asInstanceOf[EthnicityBorrowerCharacteristic]
      val oE = originalC.find(_.isInstanceOf[EthnicityBorrowerCharacteristic]).get.asInstanceOf[EthnicityBorrowerCharacteristic]
      val cE = oE + nE

      val nM = newC.find(_.isInstanceOf[MinorityStatusBorrowerCharacteristic]).get.asInstanceOf[MinorityStatusBorrowerCharacteristic]
      val oM = originalC.find(_.isInstanceOf[MinorityStatusBorrowerCharacteristic]).get.asInstanceOf[MinorityStatusBorrowerCharacteristic]
      val cM = oM + nM

      ApplicantIncome(income.applicantIncome, List(cR, cE, cM))
    })

  }
}

object N5X {

  def generate[ec: EC, mat: MAT, as: AS](
    report: A5XReportCreator,
    larSource: Source[LoanApplicationRegister, NotUsed]
  ): Future[N5X] = {

    val fipsList = MsaIncomeLookup.values.map(_.fips).filterNot(_ == 99999)

    val agReports = fipsList.map(fipsCode => report.generate(larSource, fipsCode))

    Future.sequence(agReports).map(seq => {
      val a = seq.head
      val initialN = N5X(a.year, a.applicantIncomes, a.total, a.table, a.description, a.reportDate)
      seq.tail.foldLeft(initialN)((n5X, a5X) => n5X + a5X)
    })
  }
}
