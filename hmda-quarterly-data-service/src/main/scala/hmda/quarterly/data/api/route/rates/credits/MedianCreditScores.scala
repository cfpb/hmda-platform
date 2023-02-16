package hmda.quarterly.data.api.route.rates.credits

import hmda.model.filing.lar.enums._
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData.GraphSeriesSummary
import hmda.quarterly.data.api.route.rates.CountRatesGraph
import hmda.quarterly.data.api.route.rates.RatesGraph._
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object MedianCreditScores extends CountRatesGraph(
  "credit",
  "credit-scores",
  BY_TYPE_TITLE,
  BY_TYPE_SUBTITLE,
  Category.BY_TYPE) {

  override protected def getSummaryByType(loanType: LoanTypeEnum, title: String, heloc: Boolean = false, conforming: Boolean = false): CancelableFuture[GraphSeriesSummary] =
    QuarterlyGraphRepo.fetchMedianCreditScoreByType(loanType, heloc, conforming)
      .map(convertToGraph(title, _)).runToFuture
}
