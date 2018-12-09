package hmda.census.query

case class CensusEntity(
    collectionYear: Int = 0,
    msaMd: Int = 0,
    state: String = "",
    county: String = "",
    tract: Int = 0,
    medianIncome: Int = 0,
    population: Int = 0,
    minorityPopulationPercent: Double = 0F,
    occupiedUnits: Int = 0,
    oneToFourFamilyUnits: Int = 0,
    tractMfi: Int = 0,
    tracttoMsaIncomePercent: Double = 0F,
    medianAge: Int = 0
) {}
