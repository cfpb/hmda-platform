package hmda.publication.reports.national

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.census.model.MsaIncomeLookup
import hmda.model.publication.reports.ApplicantIncomeEnum._
import hmda.model.publication.reports._
import hmda.publication.reports._
import hmda.publication.reports.aggregate.A52
import hmda.publication.reports.util.ReportsMetaDataLookup
import hmda.query.model.filing.LoanApplicationRegisterQuery

import scala.concurrent.Future

case class N52(
                year: Int = 0,
                reportDate: String = "",
                applicantIncomes: List[ApplicantIncome] = List(),
                total: List[Disposition] = List(),
                table: String = N52.metaData.reportTable,
                description: String = N52.metaData.description
              ) extends NationalAggregateReport {
  def +(a52: A52): N52 = {
    val combinedIncomes = a52.applicantIncomes.map(income => {
      val bc = applicantIncomes.find(i => i.applicantIncome == income.applicantIncome).get.borrowerCharacteristics

      val originalRc = bc.find(_.isInstanceOf[RaceBorrowerCharacteristic]).get.asInstanceOf[RaceBorrowerCharacteristic]
      val additionalRc = income.borrowerCharacteristics.find(_.isInstanceOf[RaceBorrowerCharacteristic]).get.asInstanceOf[RaceBorrowerCharacteristic]

      val originalEc = bc.find(_.isInstanceOf[EthnicityBorrowerCharacteristic]).get.asInstanceOf[EthnicityBorrowerCharacteristic]
      val additionalEc = income.borrowerCharacteristics.find(_.isInstanceOf[EthnicityBorrowerCharacteristic]).get.asInstanceOf[EthnicityBorrowerCharacteristic]

      val originalMsc = bc.find(_.isInstanceOf[MinorityStatusBorrowerCharacteristic]).get.asInstanceOf[MinorityStatusBorrowerCharacteristic]
      val additionalMsc = income.borrowerCharacteristics.find(_.isInstanceOf[MinorityStatusBorrowerCharacteristic]).get.asInstanceOf[MinorityStatusBorrowerCharacteristic]

      ApplicantIncome(income.applicantIncome, List(
        originalRc + additionalRc,
        originalEc + additionalEc,
        originalMsc + additionalMsc
      ))
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

    fipsList.foldLeft(N52())((n, f) => {
      val a52 = A52.generate(larSource, f)
      a52.map{report => n + report}
    })
  }
}
