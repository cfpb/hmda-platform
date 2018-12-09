package hmda.census.api.http

import hmda.census.query.CensusEntity
import hmda.model.census.Census

object CensusConverter {

  def convert(entity: CensusEntity): Census = {
    Census(
      entity.collectionYear,
      entity.msaMd,
      entity.state,
      entity.county,
      entity.tract,
      entity.medianIncome,
      entity.population,
      entity.minorityPopulationPercent,
      entity.occupiedUnits,
      entity.oneToFourFamilyUnits,
      entity.tractMfi,
      entity.tracttoMsaIncomePercent,
      entity.medianAge
    )
  }

}
