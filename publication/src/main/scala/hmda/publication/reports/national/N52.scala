package hmda.publication.reports.national

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.census.model.MsaIncomeLookup
import hmda.model.publication.reports._
import hmda.publication.reports._
import hmda.publication.reports.aggregate.A52
import hmda.publication.reports.util.ReportUtil._
import hmda.publication.reports.util.ReportsMetaDataLookup
import hmda.query.model.filing.LoanApplicationRegisterQuery

import scala.concurrent.Future

case class N52(
    year: Int = 0,
    reportDate: String = formattedCurrentDate,
    applicantIncomes: List[ApplicantIncome] = List(),
    total: List[Disposition] = List(),
    table: String = N52.metaData.reportTable,
    description: String = N52.metaData.description
) extends NationalAggregateReport {
  def +(a52: A52): N52 = {

    val combinedIncomes = a52.applicantIncomes.map(income => {
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

    val combinedDispositions = a52.total.map(disposition => {
      val originalDisposition = total.find(d => d.disposition == disposition.disposition).get
      disposition + originalDisposition
    })

    N52(year, reportDate, combinedIncomes, combinedDispositions, table, description)
  }
}

object N52 {
  val metaData = ReportsMetaDataLookup.values("N52")
  val dispositions = metaData.dispositions

  // Table filters:
  // Loan Type 1
  // Property Type 1,2
  // Purpose of Loan 1
  def generate[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegisterQuery, NotUsed]): Future[N52] = {
    val fipsList = MsaIncomeLookup.values.map(_.fips)

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
