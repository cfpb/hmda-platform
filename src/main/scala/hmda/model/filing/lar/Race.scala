package hmda.model.filing.lar

import hmda.model.filing.lar.enums.{RaceEnum, RaceObservedEnum}

case class Race(
    race1: RaceEnum,
    race2: RaceEnum,
    race3: RaceEnum,
    race4: RaceEnum,
    race5: RaceEnum,
    otherNativeRace: String,
    otherAsianRace: String,
    otherPacificIslanderRace: String,
    raceObserved: RaceObservedEnum
)
