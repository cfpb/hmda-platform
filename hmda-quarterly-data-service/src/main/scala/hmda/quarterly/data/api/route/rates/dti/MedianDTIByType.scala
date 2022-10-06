package hmda.quarterly.data.api.route.rates.dti

import hmda.model.filing.lar.enums.LoanTypeEnum
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData.GraphSeriesSummary
import hmda.quarterly.data.api.route.rates.RatesGraph
import hmda.quarterly.data.api.route.rates.RatesGraph.BY_TYPE
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object MedianDTIByType extends RatesGraph("dti", "dti") {

  override protected def title: String = BY_TYPE_TITLE
  override protected def subtitle: String = BY_TYPE_SUBTITLE
  override protected def summaryType: RatesGraph.Value = BY_TYPE

  override protected def getSummaryByType(loanType: LoanTypeEnum, title: String, heloc: Boolean, conforming: Boolean): CancelableFuture[GraphSeriesSummary] =
    QuarterlyGraphRepo.fetchMedianDTIByType(loanType, heloc, conforming)
      .map(convertToGraph(title, _)).runToFuture
}
