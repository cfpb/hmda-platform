package hmda.quarterly.data.api.route.rates.interests

import hmda.model.filing.lar.enums.Conventional
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData.GraphSeriesSummary
import hmda.quarterly.data.api.route.rates.InterestRatesGraph
import hmda.quarterly.data.api.route.rates.RatesGraph._
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object MedianInterestRatesCCByRaceLoanPurposeHome extends InterestRatesGraph(
  "interest",
  "interest-rates-cc-re-loan-purpose-home",
  CC_BY_RACE_TITLE,
  CC_BY_RACE_SUBTITLE,
  Category.BY_RACE) {

  override protected def getSummaryByRace(title: String, race: String): CancelableFuture[GraphSeriesSummary] =
    QuarterlyGraphRepo.fetchMedianInterestRatesByTypeByRaceLoanPurposeHome(Conventional, race, heloc = false, conforming = true)
      .map(convertToGraph(title, _)).runToFuture
}
