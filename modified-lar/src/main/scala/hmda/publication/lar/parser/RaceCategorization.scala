package hmda.publication.lar

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._


object RaceCategorization {


  def assignRaceCategorization(lar: LoanApplicationRegister): String = {


    val race = lar.applicant.race
    val coRace = lar.coApplicant.race

    val raceFields = Array(race.race1,
      race.race2,
      race.race3,
      race.race4,
      race.race5)
    val coRaceFields = Array(coRace.race1,
      coRace.race2,
      coRace.race3,
      coRace.race4,
      coRace.race5)

    val asianEnums= Array(Asian,
      AsianIndian,
      Chinese,
      Filipino,
      Japanese,
      Korean,
      Vietnamese,
      OtherAsian)



    val hawaiianIslanderEnums= Array(NativeHawaiianOrOtherPacificIslander,
      NativeHawaiian,
      GuamanianOrChamorro,
      Samoan,
      OtherPacificIslander)

    val isRaceEmpty =  race.race2 ==EmptyRaceValue &&
      race.race3 ==EmptyRaceValue &&
      race.race4 ==EmptyRaceValue &&
      race.race5 ==EmptyRaceValue

    val isCoRaceEmpty =  coRace.race2 ==EmptyRaceValue &&
      coRace.race3 ==EmptyRaceValue &&
      coRace.race4 ==EmptyRaceValue &&
      coRace.race5 ==EmptyRaceValue


      val isWhite = race.race1==White &&
        isRaceEmpty &&(
        coRace.race1==White ||
          coRace.race1==RaceInformationNotProvided ||
          coRace.race1==RaceNotApplicable ||
        coRace.race1 ==RaceNoCoApplicant
      )&& isCoRaceEmpty

    val isCoWhite = coRace.race1 == White ||
      coRace.race2 ==White ||
      coRace.race3 ==White ||
      coRace.race4 ==White ||
      coRace.race5 ==White


      if (asianEnums.contains(race.race1)){
        Asian.description
      }
      else if (hawaiianIslanderEnums.contains((race.race1))){
        NativeHawaiianOrOtherPacificIslander.description
      }

      else if (race.race1==EmptyRaceValue){
        "Free Form Text Only"
      }

      else if (race.race1 == RaceInformationNotProvided ||
        race.race1 == RaceNotApplicable){
        "Race not available"}

        //American Indian
      else if (race.race1 == AmericanIndianOrAlaskaNative &&
      !isCoWhite){
        AmericanIndianOrAlaskaNative.description}


        //Asian
      else if (asianEnums.contains(race.race1)&&
        isRaceEmpty && !isCoWhite){
        Asian.description
      }
        //Afrcan American
      else if (race.race1 == BlackOrAfricanAmerican &&
        isRaceEmpty && !isCoWhite){
        BlackOrAfricanAmerican.description
      }

      else if (hawaiianIslanderEnums.contains(race.race1) &&
        isRaceEmpty && !isCoWhite){
        NativeHawaiianOrOtherPacificIslander.description
      }
      else if (race.race1 == AmericanIndianOrAlaskaNative&&
               race.race2==White&&
      race.race3==EmptyRaceValue &&
      race.race4 == EmptyRaceValue &&
      race.race5 == EmptyRaceValue &&
      !isCoWhite){
        AmericanIndianOrAlaskaNative.description
      }
      else if (asianEnums.contains(race.race1)&&race.race2==White&&
        race.race3==EmptyRaceValue &&
        race.race4 == EmptyRaceValue &&
        race.race5 == EmptyRaceValue &&
        !isCoWhite
      ){
        Asian.description
      }

      else if (race.race1 == BlackOrAfricanAmerican&&race.race2==White&&
        race.race3==EmptyRaceValue &&
        race.race4 == EmptyRaceValue &&
        race.race5 == EmptyRaceValue &&
        !isCoWhite
      ){
        BlackOrAfricanAmerican.description
      }

      else if (race.race1 == NativeHawaiianOrOtherPacificIslander &&race.race2==White&&
        race.race3==EmptyRaceValue &&
        race.race4 == EmptyRaceValue &&
        race.race5 == EmptyRaceValue &&
        !isCoWhite
      ){
        NativeHawaiianOrOtherPacificIslander.description
      }

      else if (raceFields.exists(asianEnums.contains)  && !isCoWhite){
        Asian.description
      }

      else if (raceFields.exists(hawaiianIslanderEnums.contains)  && !isCoWhite){
        NativeHawaiianOrOtherPacificIslander.description
      }

      else if (raceFields.exists(hawaiianIslanderEnums.contains) && raceFields.exists(asianEnums.contains) && !isCoWhite){
        "2 or more minority races"
      }

      else if (race.race1 ==White &&
        isRaceEmpty &&
        coRace.race1 == White ||
        coRace.race1 == RaceInformationNotProvided ||
        coRace.race1 == RaceNotApplicable ||
        coRace.race1 == RaceNoCoApplicant &&(
        coRace.race2 == EmptyRaceValue &&
          coRace.race3 == EmptyRaceValue &&
          coRace.race4 == EmptyRaceValue &&
          coRace.race5 == EmptyRaceValue)){
        White.description}
      else if ( (raceFields.exists(hawaiianIslanderEnums.contains)||
                 raceFields.exists(asianEnums.contains)||
         race.race1==AmericanIndianOrAlaskaNative||
        race.race1== BlackOrAfricanAmerican) &&
        isCoWhite
          ){
        "Joint"
      }
      else if ( (coRaceFields.exists(hawaiianIslanderEnums.contains)||
        coRaceFields.exists(asianEnums.contains) ||
        coRaceFields== AmericanIndianOrAlaskaNative||
        race.race1== BlackOrAfricanAmerican) &&
        isWhite
      ){
        "Joint"
      }
      else
        "Not Determined"
  }


}
