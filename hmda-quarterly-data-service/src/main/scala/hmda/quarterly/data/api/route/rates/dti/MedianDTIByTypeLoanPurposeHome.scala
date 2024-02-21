package hmda.quarterly.data.api.route.rates.dti

import hmda.model.filing.lar.enums.LoanTypeEnum
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData.GraphSeriesSummary
import hmda.quarterly.data.api.route.rates.RatesGraph
import hmda.quarterly.data.api.route.rates.RatesGraph._
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object MedianDTIByTypeLoanPurposeHome extends RatesGraph(
  "dti",
  "dti-loan-purpose-home",
  BY_TYPE_TITLE,
  BY_TYPE_SUBTITLE,
  Category.BY_TYPE_NO_HELOC) {

  override protected def getSummaryByType(loanType: LoanTypeEnum, title: String, heloc: Boolean, conforming: Boolean): CancelableFuture[GraphSeriesSummary] =
    QuarterlyGraphRepo.fetchMedianDTIByTypeLoanPurposeHome(loanType, heloc, conforming)
      .map(convertToGraph(title, _)).runToFuture
}
