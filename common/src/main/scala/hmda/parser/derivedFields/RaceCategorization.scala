package hmda.parser.derivedFields

import hmda.model.filing.lar.{ LoanApplicationRegister, Race }
import hmda.model.filing.lar.enums._

object RaceCategorization {

  def assignRaceCategorization(lar: LoanApplicationRegister): String = {

    val race   = lar.applicant.race
    val coRace = lar.coApplicant.race

    val raceFields =
      Array(race.race1, race.race2, race.race3, race.race4, race.race5)
    val coRaceFields = Array(coRace.race1, coRace.race2, coRace.race3, coRace.race4, coRace.race5)

    val asianEnums = Seq(Asian, AsianIndian, Chinese, Filipino, Japanese, Korean, Vietnamese, OtherAsian)
    val hawaiianIslanderEnums =
      Seq(NativeHawaiianOrOtherPacificIslander, NativeHawaiian, GuamanianOrChamorro, Samoan, OtherPacificIslander)

    if (race.race1 == EmptyRaceValue) {
      "Free Form Text Only"
    } else if (race.race1 == RaceInformationNotProvided ||
               race.race1 == RaceNotApplicable) {
      "Race Not Available"
    }
    // Two or more minorities
    else if (moreThanOneMinority(raceFields, asianEnums, hawaiianIslanderEnums) &&
             !AnyApplicantWhite(coRace)) {
      "2 or more minority races"
    }

    // Joint
    else if (AnyApplicantAMinority(raceFields, asianEnums, hawaiianIslanderEnums) &&
             AnyApplicantWhite(coRace)) {
      "Joint"
    } else if (AnyApplicantAMinority(coRaceFields, asianEnums, hawaiianIslanderEnums) &&
               race.race1 == White &&
               isRaceTwoToFiveEmpty(race)) {
      "Joint"
    }

    //White
    else if (race.race1 == White &&
             isRaceTwoToFiveEmpty(race) &&
             isRaceTwoToFiveEmpty(coRace) &&
             (coRace.race1 == White ||
             coRace.race1 == RaceInformationNotProvided ||
             coRace.race1 == RaceNotApplicable ||
             coRace.race1 == RaceNoCoApplicant)) {
      White.description
    }

    //American Indian Or Alaska Native
    else if (race.race1 == AmericanIndianOrAlaskaNative &&
             isRaceTwoToFiveEmpty(race) &&
             !AnyApplicantWhite(coRace)) {
      AmericanIndianOrAlaskaNative.description
    } else if ((race.race1 == AmericanIndianOrAlaskaNative && race.race2 == White) ||
               (race.race2 == AmericanIndianOrAlaskaNative && race.race1 == White)) {
      if (isRaceThreeToFiveEmpty(race) && !AnyApplicantWhite(coRace)) {
        AmericanIndianOrAlaskaNative.description
      } else {
        "Joint"
      }
    }

    //Asian
    else if (asianEnums.contains(race.race1) &&
             isRaceTwoToFiveEmpty(race) &&
             !AnyApplicantWhite(coRace)) {
      Asian.description
    } else if (OnlyAsian(raceFields, asianEnums, hawaiianIslanderEnums) &&
               !AnyApplicantWhite(coRace)) {
      Asian.description
    } else if ((asianEnums.contains(race.race1) && race.race2 == White) ||
               (asianEnums.contains(race.race2) && race.race1 == White)) {
      if (isRaceThreeToFiveEmpty(race) && !AnyApplicantWhite(coRace)) {
        Asian.description
      } else {
        "Joint"
      }
    }

    //Native Hawaiian or Other Pacific Islander
    else if (hawaiianIslanderEnums.contains(race.race1) &&
             isRaceTwoToFiveEmpty(race) &&
             !AnyApplicantWhite(race)) {
      NativeHawaiianOrOtherPacificIslander.description
    } else if (OnlyNativeHawaiianOrOtherPacificIslander(raceFields, asianEnums, hawaiianIslanderEnums) &&
               !AnyApplicantWhite(coRace)) {
      NativeHawaiianOrOtherPacificIslander.description
    } else if ((hawaiianIslanderEnums.contains(race.race1) && race.race2 == White) ||
               (hawaiianIslanderEnums.contains(race.race2) && race.race1 == White)) {
      if (isRaceThreeToFiveEmpty(race) && !AnyApplicantWhite(coRace)) {
        NativeHawaiianOrOtherPacificIslander.description
      } else {
        "Joint"
      }
    }
    //Black Or AfricanAmerican
    else if (race.race1 == BlackOrAfricanAmerican &&
             isRaceTwoToFiveEmpty(race) &&
             !AnyApplicantWhite(coRace)) {
      BlackOrAfricanAmerican.description
    } else if ((race.race1 == BlackOrAfricanAmerican && race.race2 == White) ||
               (race.race2 == BlackOrAfricanAmerican && race.race1 == White)) {
      if (isRaceThreeToFiveEmpty(race) && !AnyApplicantWhite(coRace)) {
        BlackOrAfricanAmerican.description
      } else {
        "Joint"
      }
    } else
      "Joint"
  }

  private def AnyApplicantAMinority(raceFields: Array[RaceEnum],
                                    asianEnums: Seq[RaceEnum with Product],
                                    hawaiianIslanderEnums: Seq[RaceEnum with Product]): Boolean =
    raceFields.exists(asianEnums.contains) ||
      raceFields.exists(hawaiianIslanderEnums.contains) ||
      raceFields.contains(BlackOrAfricanAmerican) |
      raceFields.contains(AmericanIndianOrAlaskaNative)

  private def OnlyNativeHawaiianOrOtherPacificIslander(raceFields: Array[RaceEnum],
                                                       asianEnums: Seq[RaceEnum with Product],
                                                       hawaiianIslanderEnums: Seq[RaceEnum with Product]): Boolean =
    !raceFields.exists(asianEnums.contains) &&
      raceFields.exists(hawaiianIslanderEnums.contains) &&
      !raceFields.contains(BlackOrAfricanAmerican) &&
      !raceFields.contains(AmericanIndianOrAlaskaNative)

  private def OnlyAsian(raceFields: Array[RaceEnum],
                        asianEnums: Seq[RaceEnum with Product],
                        hawaiianIslanderEnums: Seq[RaceEnum with Product]): Boolean =
    raceFields.exists(asianEnums.contains) &&
      !raceFields.exists(hawaiianIslanderEnums.contains) &&
      !raceFields.contains(BlackOrAfricanAmerican) &&
      !raceFields.contains(AmericanIndianOrAlaskaNative)

  private def moreThanOneMinority(raceFields: Array[RaceEnum],
                                  asianEnums: Seq[RaceEnum with Product],
                                  hawaiianIslanderEnums: Seq[RaceEnum with Product]): Boolean = {
    val numberOfMinorityFields = List(
      raceFields.exists(hawaiianIslanderEnums.contains),
      raceFields.exists(asianEnums.contains),
      raceFields.contains(BlackOrAfricanAmerican),
      raceFields.contains(AmericanIndianOrAlaskaNative)
    ).filter(x => x).size
    if (numberOfMinorityFields > 1) {
      true
    } else {
      false
    }
  }

  private def AnyApplicantWhite(race: Race): Boolean =
    (race.race1 == White ||
      race.race2 == White ||
      race.race3 == White ||
      race.race5 == White)

  private def isRaceThreeToFiveEmpty(race: Race): Boolean =
    race.race3 == EmptyRaceValue &&
      race.race4 == EmptyRaceValue &&
      race.race5 == EmptyRaceValue

  private def isRaceTwoToFiveEmpty(race: Race): Boolean =
    race.race2 == EmptyRaceValue &&
      race.race3 == EmptyRaceValue &&
      race.race4 == EmptyRaceValue &&
      race.race5 == EmptyRaceValue
}
