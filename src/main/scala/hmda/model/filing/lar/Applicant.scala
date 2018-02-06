package hmda.model.filing.lar

case class Applicant(
    ethnicity1: Ethnicity,
    ethnicity2: Ethnicity,
    ethnicity3: Ethnicity,
    ethnicity4: Ethnicity,
    ethnicity5: Ethnicity,
    otherHispanicOrLatino: String,
    ethnicityObserved: EthnicityObserved,
    race1: Race,
    race2: Race,
    race3: Race,
    race4: Race,
    race5: Race,
    raceObserved: RaceObserved,
    otherNativeRace: String,
    otherAsianRace: String,
    otherPacificIslanderRace: String
)
