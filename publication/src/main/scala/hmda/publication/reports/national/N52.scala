package hmda.publication.reports.national

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.census.model.MsaIncomeLookup
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.publication.reports._
import hmda.publication.reports._
import hmda.publication.reports.aggregate.{ A52, A5X }
import hmda.publication.reports.util.ReportUtil._
import hmda.publication.reports.util.ReportsMetaDataLookup

import scala.concurrent.Future

case class N52(
    year: Int = 0,
    reportDate: String = formattedCurrentDate,
    applicantIncomes: List[ApplicantIncome] = List(),
    total: List[Disposition] = List(),
    table: String = N52.metaData.reportTable,
    description: String = N52.metaData.description
) extends NationalAggregateReport {
  def +(a5X: A5X): N52 = {
    if (applicantIncomes.isEmpty) {
      N52(a5X.year, a5X.reportDate, a5X.applicantIncomes, a5X.total)
    } else {
      val combinedIncomes = a5X.applicantIncomes.map(income => {
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

      val combinedDispositions = a5X.total.map(disposition => {
        val originalDisposition = total.find(d => d.disposition == disposition.disposition).get
        disposition + originalDisposition
      })

      N52(year, reportDate, combinedIncomes, combinedDispositions)
    }
  }
}

object N52 {
  val metaData = ReportsMetaDataLookup.values("N52")
  val dispositions = metaData.dispositions

  // Table filters:
  // Loan Type 1
  // Property Type 1,2
  // Purpose of Loan 1
  def generate[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegister, NotUsed]): Future[N52] = {
    val fipsList = MsaIncomeLookup.values.map(_.fips).filterNot(_ == 99999)

    val a52List = fipsList.map(fipsCode => A52.generate(larSource, fipsCode))
    val n52f = Future.sequence(a52List).map(seq => {
      seq.foldLeft(N52())((n52, a52) => n52 + a52)
    })

    for {
      n <- n52f
      y <- calculateYear(larSource)
    } yield N52(y, n.reportDate, n.applicantIncomes, n.total)
  }
}
