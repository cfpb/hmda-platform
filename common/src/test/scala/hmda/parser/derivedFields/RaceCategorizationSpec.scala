package hmda.parser.derivedFields

import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }
import hmda.model.filing.lar.enums._
import hmda.model.filing.lar.LarGenerators._
import hmda.parser.derivedFields.RaceCategorization._
import hmda.model.filing.lar.Race

class RaceCategorizationSpec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  property("Race Categorization must identify race categories") {
    val multiRace                = Race(BlackOrAfricanAmerican, Asian, EmptyRaceValue, EmptyRaceValue, EmptyRaceValue)
    val asianRace                = Race(Asian, EmptyRaceValue, EmptyRaceValue, EmptyRaceValue, EmptyRaceValue)
    val asianWhiteRace           = Race(Asian, White, EmptyRaceValue, EmptyRaceValue, EmptyRaceValue)
    val blackRace                = Race(BlackOrAfricanAmerican, EmptyRaceValue, EmptyRaceValue, EmptyRaceValue, EmptyRaceValue)
    val blackWhiteRace           = Race(BlackOrAfricanAmerican, White, EmptyRaceValue, EmptyRaceValue, EmptyRaceValue)
    val americanIndianRace       = Race(AmericanIndianOrAlaskaNative, EmptyRaceValue, EmptyRaceValue, EmptyRaceValue, EmptyRaceValue)
    val americanIndianWhiteRace  = Race(AmericanIndianOrAlaskaNative, White, EmptyRaceValue, EmptyRaceValue, EmptyRaceValue)
    val pacificIslanderRace      = Race(NativeHawaiianOrOtherPacificIslander, EmptyRaceValue, EmptyRaceValue, EmptyRaceValue, EmptyRaceValue)
    val pacificIslanderWhiteRace = Race(NativeHawaiianOrOtherPacificIslander, White, EmptyRaceValue, EmptyRaceValue, EmptyRaceValue)
    val whiteRace                = Race(White, EmptyRaceValue, EmptyRaceValue, EmptyRaceValue, EmptyRaceValue)
    val emptyRace                = Race(EmptyRaceValue, EmptyRaceValue, EmptyRaceValue, EmptyRaceValue, EmptyRaceValue)
    val noCoRace                 = Race(RaceNoCoApplicant, EmptyRaceValue, EmptyRaceValue, EmptyRaceValue, EmptyRaceValue)
    val notProvidedRace          = Race(RaceInformationNotProvided, EmptyRaceValue, EmptyRaceValue, EmptyRaceValue, EmptyRaceValue)
    val naRace                   = Race(RaceNotApplicable, EmptyRaceValue, EmptyRaceValue, EmptyRaceValue, EmptyRaceValue)
    forAll(larGen) { lar =>
      val notProvidedLar      = lar.copy(applicant = lar.applicant.copy(race = notProvidedRace))
      val naLar               = lar.copy(applicant = lar.applicant.copy(race = naRace))
      val emptyLar            = lar.copy(applicant = lar.applicant.copy(race = emptyRace))
      val multiRaceLar        = lar.copy(applicant = lar.applicant.copy(race = multiRace))
      val multipleMinorityLar = multiRaceLar.copy(coApplicant = multiRaceLar.coApplicant.copy(race = multiRace))
      val blackLar            = lar.copy(applicant = lar.applicant.copy(race = blackRace), coApplicant = lar.coApplicant.copy(race = noCoRace))
      val blackLar2           = lar.copy(applicant = lar.applicant.copy(race = blackWhiteRace), coApplicant = lar.coApplicant.copy(race = noCoRace))
      val americanIndianLar1 =
        lar.copy(applicant = lar.applicant.copy(race = americanIndianRace), coApplicant = lar.coApplicant.copy(race = noCoRace))
      val americanIndianLar2 =
        lar.copy(applicant = lar.applicant.copy(race = americanIndianWhiteRace), coApplicant = lar.coApplicant.copy(race = noCoRace))
      val asianLar1 = lar.copy(applicant = lar.applicant.copy(race = asianRace), coApplicant = lar.coApplicant.copy(race = noCoRace))
      val asianLar2 = lar.copy(applicant = lar.applicant.copy(race = asianWhiteRace), coApplicant = lar.coApplicant.copy(race = noCoRace))
      val whiteLar  = lar.copy(applicant = lar.applicant.copy(race = whiteRace), coApplicant = lar.coApplicant.copy(race = noCoRace))
      val pacificIslanderLar1 =
        lar.copy(applicant = lar.applicant.copy(race = pacificIslanderRace), coApplicant = lar.coApplicant.copy(race = noCoRace))
      val pacificIslanderLar2 =
        lar.copy(applicant = lar.applicant.copy(race = pacificIslanderWhiteRace), coApplicant = lar.coApplicant.copy(race = noCoRace))

      val jointLar1 =
        lar.copy(applicant = lar.applicant.copy(race = pacificIslanderRace), coApplicant = lar.coApplicant.copy(race = whiteRace))
      val jointLar2 = lar.copy(applicant = lar.applicant.copy(race = whiteRace), coApplicant = lar.coApplicant.copy(race = asianRace))
      val jointLar3 =
        lar.copy(applicant = lar.applicant.copy(race = americanIndianRace), coApplicant = lar.coApplicant.copy(race = whiteRace))
      val jointLar4 = lar.copy(applicant = lar.applicant.copy(race = blackRace), coApplicant = lar.coApplicant.copy(race = whiteRace))
      val jointLar5 = lar.copy(applicant = lar.applicant.copy(race = asianRace), coApplicant = lar.coApplicant.copy(race = whiteRace))

      val pacificIslanderJointLar =
        lar.copy(applicant = lar.applicant.copy(race = pacificIslanderWhiteRace), coApplicant = lar.coApplicant.copy(race = whiteRace))
      val americanIndianJointLar =
        lar.copy(applicant = lar.applicant.copy(race = americanIndianWhiteRace), coApplicant = lar.coApplicant.copy(race = whiteRace))
      val asianJointLar =
        lar.copy(applicant = lar.applicant.copy(race = asianWhiteRace), coApplicant = lar.coApplicant.copy(race = whiteRace))
      val blackJointLar =
        lar.copy(applicant = lar.applicant.copy(race = blackWhiteRace), coApplicant = lar.coApplicant.copy(race = whiteRace))

      assignRaceCategorization(notProvidedLar) mustBe "Race Not Available"
      assignRaceCategorization(naLar) mustBe "Race Not Available"
      assignRaceCategorization(emptyLar) mustBe "Free Form Text Only"
      assignRaceCategorization(multipleMinorityLar) mustBe "2 or more minority races"
      assignRaceCategorization(blackLar) mustBe BlackOrAfricanAmerican.description
      assignRaceCategorization(blackLar2) mustBe BlackOrAfricanAmerican.description
      assignRaceCategorization(americanIndianLar1) mustBe AmericanIndianOrAlaskaNative.description
      assignRaceCategorization(americanIndianLar2) mustBe AmericanIndianOrAlaskaNative.description
      assignRaceCategorization(asianLar1) mustBe Asian.description
      assignRaceCategorization(asianLar2) mustBe Asian.description
      assignRaceCategorization(whiteLar) mustBe White.description
      assignRaceCategorization(pacificIslanderLar1) mustBe NativeHawaiianOrOtherPacificIslander.description
      assignRaceCategorization(pacificIslanderLar2) mustBe NativeHawaiianOrOtherPacificIslander.description

      assignRaceCategorization(jointLar1) mustBe "Joint"
      assignRaceCategorization(jointLar2) mustBe "Joint"
      assignRaceCategorization(jointLar3) mustBe "Joint"
      assignRaceCategorization(jointLar4) mustBe "Joint"
      assignRaceCategorization(jointLar5) mustBe "Joint"

      assignRaceCategorization(pacificIslanderJointLar) mustBe "Joint"
      assignRaceCategorization(americanIndianJointLar) mustBe "Joint"
      assignRaceCategorization(asianJointLar) mustBe "Joint"
      assignRaceCategorization(blackJointLar) mustBe "Joint"
    }
  }
}