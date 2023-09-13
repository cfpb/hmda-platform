package hmda.quarterly.data.api.route.rates.dti

import hmda.model.filing.lar.enums.Conventional
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData.GraphSeriesSummary
import hmda.quarterly.data.api.route.rates.RatesGraph
import hmda.quarterly.data.api.route.rates.RatesGraph._
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object MedianDTICCByRaceLoanPurposeHome extends RatesGraph(
  "dti",
  "dti-cc-re-loan-purpose-home",
  CC_BY_RACE_TITLE,
  CC_BY_RACE_SUBTITLE,
  Category.BY_RACE) {

  override protected def getSummaryByRace(title: String, race: String): CancelableFuture[GraphSeriesSummary] =
    QuarterlyGraphRepo.fetchMedianDTIByTypeByRaceLoanPurposeHome(Conventional, race, heloc = false, conforming = true)
      .map(convertToGraph(title, _)).runToFuture
}
