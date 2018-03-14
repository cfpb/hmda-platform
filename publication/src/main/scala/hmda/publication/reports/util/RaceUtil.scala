package hmda.publication.reports.util

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.publication.reports.RaceEnum
import hmda.model.publication.reports.RaceEnum._

object RaceUtil {

  def filterRace(larSource: Source[LoanApplicationRegister, NotUsed], race: RaceEnum): Source[LoanApplicationRegister, NotUsed] = {
    race match {
      case AmericanIndianOrAlaskaNative =>
        larSource.filter { lar =>
          lar.applicant.race1 == 1 && coApplicantNonWhite(lar) &&
            (applicantRace2Thru5Blank(lar) || lar.applicant.race2 == "5")
        }

      case Asian =>
        larSource.filter { lar =>
          lar.applicant.race1 == 2 && coApplicantNonWhite(lar) &&
            (applicantRace2Thru5Blank(lar) || lar.applicant.race2 == "5")
        }

      case BlackOrAfricanAmerican =>
        larSource.filter { lar =>
          lar.applicant.race1 == 3 && coApplicantNonWhite(lar) &&
            (applicantRace2Thru5Blank(lar) || lar.applicant.race2 == "5")
        }

      case HawaiianOrPacific =>
        larSource.filter { lar =>
          lar.applicant.race1 == 4 && coApplicantNonWhite(lar) &&
            (applicantRace2Thru5Blank(lar) || lar.applicant.race2 == "5")
        }

      case White =>
        larSource.filter { lar =>
          lar.applicant.race1 == 5 && applicantRace2Thru5Blank(lar) && coApplicantNonMinority(lar)
        }

      case TwoOrMoreMinority =>
        larSource.filter(lar => applicantTwoOrMoreMinorities(lar) && coApplicantNonWhite(lar))

      case JointRace =>
        larSource.filter { lar =>
          (applicantOneOrMoreMinorities(lar) || coApplicantOneOrMoreMinorities(lar)) &&
            (applicantWhite(lar) || coApplicantWhite(lar))
        }

      case NotProvided =>
        larSource.filter(lar => lar.applicant.race1 == 6 || lar.applicant.race1 == 7)

    }
  }

  private def applicantRace2Thru5Blank(lar: LoanApplicationRegister): Boolean = {
    lar.applicant.race2 == "" &&
      lar.applicant.race3 == "" &&
      lar.applicant.race4 == "" &&
      lar.applicant.race5 == ""
  }

  private def applicantWhite(lar: LoanApplicationRegister): Boolean = {
    lar.applicant.race1 == 5 &&
      lar.applicant.race2 == "" &&
      lar.applicant.race3 == "" &&
      lar.applicant.race4 == "" &&
      lar.applicant.race5 == ""
  }

  private def coApplicantWhite(lar: LoanApplicationRegister): Boolean = {
    lar.applicant.coRace1 == 5 &&
      lar.applicant.coRace2 == "" &&
      lar.applicant.coRace3 == "" &&
      lar.applicant.coRace4 == "" &&
      lar.applicant.coRace5 == ""
  }

  private def coApplicantNonWhite(lar: LoanApplicationRegister): Boolean = {
    lar.applicant.coRace1 != 5 &&
      lar.applicant.coRace2 != "5" &&
      lar.applicant.coRace3 != "5" &&
      lar.applicant.coRace4 != "5" &&
      lar.applicant.coRace5 != "5"
  }

  private def coApplicantNonMinority(lar: LoanApplicationRegister): Boolean = {
    val race1NonMinority = lar.applicant.coRace1 == 5 || lar.applicant.coRace1 == 6 || lar.applicant.coRace1 == 7 || lar.applicant.coRace1 == 8
    race1NonMinority &&
      (lar.applicant.race2 == "5" || lar.applicant.race2 == "") &&
      (lar.applicant.race3 == "5" || lar.applicant.race3 == "") &&
      (lar.applicant.race4 == "5" || lar.applicant.race4 == "") &&
      (lar.applicant.race5 == "5" || lar.applicant.race5 == "")
  }

  private def applicantTwoOrMoreMinorities(lar: LoanApplicationRegister): Boolean = {
    lar.applicant.race1 != 5 &&
      ((lar.applicant.race2 != "" && lar.applicant.race2 != "5") ||
        (lar.applicant.race3 != "" && lar.applicant.race3 != "5") ||
        (lar.applicant.race4 != "" && lar.applicant.race4 != "5") ||
        (lar.applicant.race5 != "" && lar.applicant.race5 != "5"))
  }

  private def applicantOneOrMoreMinorities(lar: LoanApplicationRegister): Boolean = {
    (lar.applicant.race1 == 1 || lar.applicant.race1 == 2 || lar.applicant.race1 == 3 || lar.applicant.race1 == 4) ||
      (lar.applicant.race2 != "" && lar.applicant.race2 != "5") ||
      (lar.applicant.race3 != "" && lar.applicant.race3 != "5") ||
      (lar.applicant.race4 != "" && lar.applicant.race4 != "5") ||
      (lar.applicant.race5 != "" && lar.applicant.race5 != "5")
  }
  private def coApplicantOneOrMoreMinorities(lar: LoanApplicationRegister): Boolean = {
    (lar.applicant.coRace1 == 1 || lar.applicant.coRace1 == 2 || lar.applicant.coRace1 == 3 || lar.applicant.coRace1 == 4) ||
      (lar.applicant.coRace2 != "" && lar.applicant.coRace2 != "5") ||
      (lar.applicant.coRace3 != "" && lar.applicant.coRace3 != "5") ||
      (lar.applicant.coRace4 != "" && lar.applicant.coRace4 != "5") ||
      (lar.applicant.coRace5 != "" && lar.applicant.coRace5 != "5")
  }

}
