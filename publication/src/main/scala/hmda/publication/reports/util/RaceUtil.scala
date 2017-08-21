package hmda.publication.reports.util

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.publication.reports.{ RaceBorrowerCharacteristic, RaceCharacteristic, RaceEnum }
import hmda.model.publication.reports.RaceEnum._
import hmda.publication.reports._
import hmda.publication.reports.util.ReportUtil.calculateDispositions
import hmda.publication.reports.util.DispositionType.DispositionType
import hmda.query.model.filing.LoanApplicationRegisterQuery

import scala.concurrent.Future

object RaceUtil {

  def filterRace(larSource: Source[LoanApplicationRegisterQuery, NotUsed], race: RaceEnum): Source[LoanApplicationRegisterQuery, NotUsed] = {
    race match {
      case AmericanIndianOrAlaskaNative =>
        larSource.filter { lar =>
          lar.race1 == 1 && coApplicantNonWhite(lar) &&
            (applicantRace2Thru5Blank(lar) || lar.race2 == "5")
        }

      case Asian =>
        larSource.filter { lar =>
          lar.race1 == 2 && coApplicantNonWhite(lar) &&
            (applicantRace2Thru5Blank(lar) || lar.race2 == "5")
        }

      case BlackOrAfricanAmerican =>
        larSource.filter { lar =>
          lar.race1 == 3 && coApplicantNonWhite(lar) &&
            (applicantRace2Thru5Blank(lar) || lar.race2 == "5")
        }

      case HawaiianOrPacific =>
        larSource.filter { lar =>
          lar.race1 == 4 && coApplicantNonWhite(lar) &&
            (applicantRace2Thru5Blank(lar) || lar.race2 == "5")
        }

      case White =>
        larSource.filter { lar =>
          lar.race1 == 5 && applicantRace2Thru5Blank(lar) && coApplicantNonMinority(lar)
        }

      case TwoOrMoreMinority =>
        larSource.filter(lar => applicantTwoOrMoreMinorities(lar) && coApplicantNonWhite(lar))

      case Joint =>
        larSource.filter { lar =>
          (applicantOneOrMoreMinorities(lar) || coApplicantOneOrMoreMinorities(lar)) &&
            (lar.race1 == 5 || coApplicantWhite(lar))
        }

      case NotProvided =>
        larSource.filter(lar => lar.race1 == 6 || lar.race1 == 7)

    }
  }

  private def applicantRace2Thru5Blank(lar: LoanApplicationRegisterQuery): Boolean = {
    lar.race2 == "" &&
      lar.race3 == "" &&
      lar.race4 == "" &&
      lar.race5 == ""
  }

  private def coApplicantWhite(lar: LoanApplicationRegisterQuery): Boolean = {
    lar.coRace1 == 5 &&
      lar.coRace2 == "" &&
      lar.coRace3 == "" &&
      lar.coRace4 == "" &&
      lar.coRace5 == ""
  }

  private def coApplicantNonWhite(lar: LoanApplicationRegisterQuery): Boolean = {
    lar.coRace1 != 5 &&
      lar.coRace2 != "5" &&
      lar.coRace3 != "5" &&
      lar.coRace4 != "5" &&
      lar.coRace5 != "5"
  }

  private def coApplicantNonMinority(lar: LoanApplicationRegisterQuery): Boolean = {
    val race1NonMinority = lar.coRace1 == 5 || lar.coRace1 == 6 || lar.coRace1 == 7 || lar.coRace1 == 8
    race1NonMinority &&
      (lar.race2 == "5" || lar.race2 == "") &&
      (lar.race3 == "5" || lar.race3 == "") &&
      (lar.race4 == "5" || lar.race4 == "") &&
      (lar.race5 == "5" || lar.race5 == "")
  }

  private def applicantTwoOrMoreMinorities(lar: LoanApplicationRegisterQuery): Boolean = {
    lar.race1 != 5 &&
      ((lar.race2 != "" && lar.race2 != "5") ||
        (lar.race3 != "" && lar.race3 != "5") ||
        (lar.race4 != "" && lar.race4 != "5") ||
        (lar.race5 != "" && lar.race5 != "5"))
  }

  private def applicantOneOrMoreMinorities(lar: LoanApplicationRegisterQuery): Boolean = {
    (lar.race1 == 1 || lar.race1 == 2 || lar.race1 == 3 || lar.race1 == 4) ||
      (lar.race2 != "" && lar.race2 != "5") ||
      (lar.race3 != "" && lar.race3 != "5") ||
      (lar.race4 != "" && lar.race4 != "5") ||
      (lar.race5 != "" && lar.race5 != "5")
  }
  private def coApplicantOneOrMoreMinorities(lar: LoanApplicationRegisterQuery): Boolean = {
    (lar.coRace1 == 1 || lar.coRace1 == 2 || lar.coRace1 == 3 || lar.coRace1 == 4) ||
      (lar.coRace2 != "" && lar.coRace2 != "5") ||
      (lar.coRace3 != "" && lar.coRace3 != "5") ||
      (lar.coRace4 != "" && lar.coRace4 != "5") ||
      (lar.coRace5 != "" && lar.coRace5 != "5")
  }

  def raceBorrowerCharacteristic[as: AS, mat: MAT, ec: EC](larSource: Source[LoanApplicationRegisterQuery, NotUsed], dispositions: List[DispositionType]): Future[RaceBorrowerCharacteristic] = {

    val larsAlaskan = filterRace(larSource, AmericanIndianOrAlaskaNative)
    val larsAsian = filterRace(larSource, Asian)
    val larsBlack = filterRace(larSource, BlackOrAfricanAmerican)
    val larsHawaiian = filterRace(larSource, HawaiianOrPacific)
    val larsWhite = filterRace(larSource, White)
    val larsTwoMinorities = filterRace(larSource, TwoOrMoreMinority)
    val larsJoint = filterRace(larSource, Joint)
    val larsNotProvided = filterRace(larSource, NotProvided)

    val dispAlaskanF = calculateDispositions(larsAlaskan, dispositions)
    val dispAsianF = calculateDispositions(larsAsian, dispositions)
    val dispBlackF = calculateDispositions(larsBlack, dispositions)
    val dispHawaiianF = calculateDispositions(larsHawaiian, dispositions)
    val dispWhiteF = calculateDispositions(larsWhite, dispositions)
    val dispTwoMinoritiesF = calculateDispositions(larsTwoMinorities, dispositions)
    val dispJointF = calculateDispositions(larsJoint, dispositions)
    val dispNotProvidedF = calculateDispositions(larsNotProvided, dispositions)

    for {
      dispAlaskan <- dispAlaskanF
      dispAsian <- dispAsianF
      dispBlack <- dispBlackF
      dispHawaiian <- dispHawaiianF
      dispWhite <- dispWhiteF
      dispTwoMinorities <- dispTwoMinoritiesF
      dispJoint <- dispJointF
      dispNotProvided <- dispNotProvidedF
    } yield {
      val alaskanCharacteristic = RaceCharacteristic(AmericanIndianOrAlaskaNative, dispAlaskan)
      val asianCharacteristic = RaceCharacteristic(Asian, dispAsian)
      val blackCharacteristic = RaceCharacteristic(BlackOrAfricanAmerican, dispBlack)
      val hawaiianCharacteristic = RaceCharacteristic(HawaiianOrPacific, dispHawaiian)
      val whiteCharacteristic = RaceCharacteristic(White, dispWhite)
      val twoOrMoreMinorityCharacteristic = RaceCharacteristic(TwoOrMoreMinority, dispTwoMinorities)
      val jointCharacteristic = RaceCharacteristic(Joint, dispJoint)
      val notProvidedCharacteristic = RaceCharacteristic(NotProvided, dispNotProvided)

      RaceBorrowerCharacteristic(
        List(
          alaskanCharacteristic,
          asianCharacteristic,
          blackCharacteristic,
          hawaiianCharacteristic,
          whiteCharacteristic,
          twoOrMoreMinorityCharacteristic,
          jointCharacteristic,
          notProvidedCharacteristic
        )
      )
    }

  }

}
