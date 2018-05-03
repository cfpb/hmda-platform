package hmda.model.filing.lar

import hmda.model.filing.lar.enums.{
  InvalidRaceCode,
  InvalidRaceObservedCode,
  RaceEnum,
  RaceObservedEnum
}

case class Race(
    race1: RaceEnum = InvalidRaceCode,
    race2: RaceEnum = InvalidRaceCode,
    race3: RaceEnum = InvalidRaceCode,
    race4: RaceEnum = InvalidRaceCode,
    race5: RaceEnum = InvalidRaceCode,
    otherNativeRace: String = "",
    otherAsianRace: String = "",
    otherPacificIslanderRace: String = "",
    raceObserved: RaceObservedEnum = InvalidRaceObservedCode
)
