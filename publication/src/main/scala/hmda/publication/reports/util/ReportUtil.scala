package hmda.publication.reports.util

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.census.model._
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.publication.reports.ApplicantIncomeEnum._
import hmda.model.publication.reports._
import hmda.model.publication.reports.RaceEnum._
import hmda.publication.reports.{AS, EC, MAT}
import hmda.util.SourceUtils

import scala.concurrent.Future
import scala.util.Try

object ReportUtil extends SourceUtils {

  def msaReport(fipsCode: String): MSAReport = {
    val cbsa = CbsaLookup.values.find(x => x.key == fipsCode).getOrElse(Cbsa())
    val stateFips = cbsa.key.substring(0, 2)
    val state = StateAbrvLookup.values.find(s => s.state == stateFips).getOrElse(StateAbrv("", "", ""))
    MSAReport(fipsCode, CbsaLookup.nameFor(fipsCode), state.stateAbrv, state.stateName)
  }

  def calculateMedianIncomeIntervals(fipsCode: Int): Array[Double] = {
    val msaIncome = MsaIncomeLookup.values.find(msa => msa.fips == fipsCode).getOrElse(MsaIncome())
    val medianIncome = msaIncome.income
    val i50 = medianIncome * 0.5
    val i80 = medianIncome * 0.8
    val i100 = medianIncome
    val i120 = medianIncome * 1.2
    Array(i50, i80, i100, i120)
  }

  def calculateDate[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegister, NotUsed]): Future[Int] = {
    collectHeadValue(larSource).map(lar => lar.actionTakenDate.toString.substring(0, 4).toInt)
  }

  def calculateDispositions[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegister, NotUsed]): Future[List[Disposition]] = {
    def incomeSum(lar: LoanApplicationRegister): Int = Try(lar.applicant.income.toInt).getOrElse(0)

    val loansOriginated = larSource.filter(lar => lar.actionTakenType == 1)
    val loansOriginatedCountF = count(loansOriginated)
    val incomeLoansOriginatedF = sum(loansOriginated, incomeSum)

    val loanApprovedButNotAccepted = larSource.filter(lar => lar.actionTakenType == 2)
    val loanApprovedButNotAcceptedCountF = count(loanApprovedButNotAccepted)
    val incomeApprovedButNotAcceptedF = sum(loanApprovedButNotAccepted, incomeSum)

    val loansDenied = larSource.filter(lar => lar.actionTakenType == 3)
    val loansDeniedCountF = count(loansDenied)
    val incomeLoansDeniedF = sum(loansDenied, incomeSum)

    val loansWithdrawn = larSource.filter(lar => lar.actionTakenType == 4)
    val loansWithdrawnCountF = count(loansWithdrawn)
    val incomeLoansWithdrawnF = sum(loansWithdrawn, incomeSum)

    val loansClosed = larSource.filter(lar => lar.actionTakenType == 5)
    val loansClosedCountF = count(loansClosed)
    val incomeLoansClosedF = sum(loansClosed, incomeSum)

    val loansPurchased = larSource.filter(lar => lar.actionTakenType == 6)
    val loansPurchasedCountF = count(loansPurchased)
    val incomeLoansPurchasedF = sum(loansPurchased, incomeSum)

    val preapprovalDenied = larSource.filter(lar => lar.actionTakenType == 7)
    val preapprovalDeniedCountF = count(preapprovalDenied)
    val incomePreapprovalDeniedF = sum(preapprovalDenied, incomeSum)

    val preapprovalAccepted = larSource.filter(lar => lar.actionTakenType == 8)
    val preapprovalAcceptedCountF = count(preapprovalAccepted)
    val incomePreapprovalAcceptedF = sum(preapprovalAccepted, incomeSum)

    for {
      loansOriginatedCount <- loansOriginatedCountF
      incomeLoansOriginated <- incomeLoansOriginatedF
      loanApprovedButNotAcceptedCount <- loanApprovedButNotAcceptedCountF
      incomeApprovedButNotAccepted <- incomeApprovedButNotAcceptedF
      loansDeniedCount <- loansDeniedCountF
      incomeLoansDenied <- incomeLoansDeniedF
      loansWithdrawnCount <- loansWithdrawnCountF
      incomeLoansWithdrawn <- incomeLoansWithdrawnF
      loansClosedCount <- loansClosedCountF
      incomeLoansClosed <- incomeLoansClosedF
      loansPurchasedCount <- loansPurchasedCountF
      incomeLoansPurchased <- incomeLoansPurchasedF
      preapprovalDeniedCount <- preapprovalDeniedCountF
      incomePreapprovalDenied <- incomePreapprovalDeniedF
      preapprovalAcceptedCount <- preapprovalAcceptedCountF
      incomePreapprovalAccepted <- incomePreapprovalAcceptedF
      receivedCount = loansOriginatedCount + loanApprovedButNotAcceptedCount + loansDeniedCount + loansWithdrawnCount +
        loansClosedCount + loansPurchasedCount + preapprovalDeniedCount + preapprovalAcceptedCount
      incomeReceivedCount = incomeLoansOriginated + incomeApprovedButNotAccepted + incomeLoansDenied +
        incomeLoansWithdrawn + incomeLoansClosed + incomeLoansPurchased + incomePreapprovalDenied + incomePreapprovalAccepted
    } yield {
      val applicationsReceived = Disposition(ActionTakenTypeEnum.ApplicationReceived, receivedCount, incomeReceivedCount)
      val loansOriginatedDisp = Disposition(ActionTakenTypeEnum.LoansOriginated, loansOriginatedCount, incomeLoansOriginated)
      val loansApprovedButNotAcceptedDisp = Disposition(ActionTakenTypeEnum.ApprovedButNotAccepted, loanApprovedButNotAcceptedCount, incomeApprovedButNotAccepted)
      val loansDenied = Disposition(ActionTakenTypeEnum.ApplicationsDenied, loansDeniedCount, incomeLoansDenied)
      val loansWithdrawn = Disposition(ActionTakenTypeEnum.ApplicationsWithdrawn, loansWithdrawnCount, incomeLoansWithdrawn)
      val loansClosed = Disposition(ActionTakenTypeEnum.ClosedForIncompleteness, loansClosedCount, incomeLoansClosed)
      val loansPurchased = Disposition(ActionTakenTypeEnum.LoanPurchased, loansPurchasedCount, incomeLoansPurchased)
      val preapprovalDenied = Disposition(ActionTakenTypeEnum.PreapprovalDenied, preapprovalDeniedCount, incomePreapprovalDenied)
      val preapprovalAccepted = Disposition(ActionTakenTypeEnum.PreapprovalApprovedButNotAccepted, preapprovalAcceptedCount, incomePreapprovalAccepted)
      List(
        applicationsReceived,
        loansOriginatedDisp,
        loansApprovedButNotAcceptedDisp,
        loansDenied,
        loansWithdrawn,
        loansClosed,
        loansPurchased,
        preapprovalDenied,
        preapprovalAccepted
      )
    }
  }

  def filterRace(larSource: Source[LoanApplicationRegister, NotUsed], race: RaceEnum): Source[LoanApplicationRegister, NotUsed] = {
    race match {
      case AmericanIndianOrAlaskaNative =>
        larSource.filter { lar =>
          (lar.applicant.race1 == 1 && applicantNonWhite(lar) && coApplicantNonWhite(lar)) ||
            (lar.applicant.race1 == 1 && lar.applicant.race2 == "5" && coApplicantNonWhite(lar))
        }

      case Asian =>
        larSource.filter { lar =>
          (lar.applicant.race1 == 2 && applicantNonWhite(lar) && coApplicantNonWhite(lar)) ||
            (lar.applicant.race1 == 2 && lar.applicant.race2 == "5" && coApplicantNonWhite(lar))
        }

      case BlackOrAfricanAmerican =>
        larSource.filter { lar =>
          (lar.applicant.race1 == 3 && applicantNonWhite(lar) && coApplicantNonWhite(lar)) ||
            (lar.applicant.race1 == 3 && lar.applicant.race2 == "5" && coApplicantNonWhite(lar))
        }

      case HawaiianOrPacific =>
        larSource.filter { lar =>
          (lar.applicant.race1 == 4 && applicantNonWhite(lar) && coApplicantNonWhite(lar)) ||
            (lar.applicant.race1 == 4 && lar.applicant.race2 == "5" && coApplicantNonWhite(lar))
        }

      case White =>
        larSource.filter(lar => lar.applicant.race1 == 5 && coApplicantNonWhite(lar))

      case TwoOrMoreMinority =>
        larSource.filter(lar => applicantTwoOrMoreMinorities(lar) && coApplicantNonWhite(lar))

      case Joint =>
        larSource.filter { lar =>
          (applicantTwoOrMoreMinorities(lar) || coApplicantTwoOrMoreMinorities(lar)) &&
            (applicantWhite(lar) || coApplicantWhite(lar))
        }

      case NotProvided =>
        larSource.filter(lar => lar.applicant.race1 == 6 || lar.applicant.race1 == 7)

    }
  }

  private def applicantWhite(lar: LoanApplicationRegister): Boolean = {
    lar.applicant.race1 == 5 &&
      lar.applicant.race2 == "" &&
      lar.applicant.race3 == "" &&
      lar.applicant.race4 == "" &&
      lar.applicant.race5 == ""
  }

  private def applicantNonWhite(lar: LoanApplicationRegister): Boolean = {
    lar.applicant.race1 != 5 &&
      lar.applicant.race2 == "" &&
      lar.applicant.race3 == "" &&
      lar.applicant.race4 == "" &&
      lar.applicant.race5 == ""
  }

  private def coApplicantWhite(lar: LoanApplicationRegister): Boolean = {
    lar.applicant.coRace1 == 5 &&
      lar.applicant.coRace2 != "5" &&
      lar.applicant.coRace3 != "5" &&
      lar.applicant.coRace4 != "5" &&
      lar.applicant.coRace5 != "5"
  }

  private def coApplicantNonWhite(lar: LoanApplicationRegister): Boolean = {
    lar.applicant.coRace1 != 5 &&
      lar.applicant.coRace2 != "5" &&
      lar.applicant.coRace3 != "5" &&
      lar.applicant.coRace4 != "5" &&
      lar.applicant.coRace5 != "5"
  }

  private def applicantTwoOrMoreMinorities(lar: LoanApplicationRegister): Boolean = {
    lar.applicant.race1 != 5 &&
      ((lar.applicant.race2 != "" && lar.applicant.race2 != "5") ||
        (lar.applicant.race3 != "" && lar.applicant.race3 != "5") ||
        (lar.applicant.race4 != "" && lar.applicant.race4 != "5") ||
        (lar.applicant.race5 != "" && lar.applicant.race5 != "5"))
  }

  private def coApplicantTwoOrMoreMinorities(lar: LoanApplicationRegister): Boolean = {
    lar.applicant.coRace1 != 5 &&
      ((lar.applicant.coRace2 != "" && lar.applicant.coRace2 != "5") ||
        (lar.applicant.coRace3 != "" && lar.applicant.coRace3 != "5") ||
        (lar.applicant.coRace4 != "" && lar.applicant.coRace4 != "5") ||
        (lar.applicant.coRace5 != "" && lar.applicant.coRace5 != "5"))
  }

  def raceBorrowerCharacteristic(larSource: Source[LoanApplicationRegister, NotUsed], applicantIncomeEnum: ApplicantIncomeEnum): Future[List[RaceCharacteristic]] = {

    val larsAlaskan = filterRace(larSource, AmericanIndianOrAlaskaNative)
    val larsAsian = filterRace(larSource, Asian)
    val larsBlack = filterRace(larSource, BlackOrAfricanAmerican)
    val larsHawaiian = filterRace(larSource, HawaiianOrPacific)
    val larsWhite = filterRace(larSource, White)
    val larsTwoMinorities = filterRace(larSource, TwoOrMoreMinority)
    val larsJoint = filterRace(larSource, Joint)
    val larsNotProvided = filterRace(larSource, NotProvided)

    val dispAlaskanF = calculateDispositions(larsAlaskan)
    val dispAsianF = calculateDispositions(larsAsian)
    val dispBlackF = calculateDispositions(larsBlack)
    val dispHawaiianF = calculateDispositions(larsHawaiian)
    val dispWhiteF = calculateDispositions(larsWhite)
    val dispTwoMinoritiesF = calculateDispositions(larsTwoMinorities)
    val dispJointF = calculateDispositions(larsJoint)
    val dispNotProvidedF = calculateDispositions(larsNotProvided)

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
    }

  }

}
