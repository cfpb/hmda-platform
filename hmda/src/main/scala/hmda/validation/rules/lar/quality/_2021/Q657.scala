package hmda.validation.rules.lar.quality._2021

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax.PredicateOps
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q657 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q657"

  override def apply(lar: LoanApplicationRegister): ValidationResult ={
    val invalidExemptCode= 1111

    (lar.property.totalUnits not equalTo(invalidExemptCode)) and
      (lar.income  not equalTo(invalidExemptCode.toString)) and
      (lar.purchaserType not equalTo(InvalidPurchaserExemptCode)) and
      (lar.hoepaStatus not equalTo(InvalidHOEPAStatusExemptCode)) and
      (lar.lienStatus not equalTo(InvalidLienStatusExemptCode)) and
      (lar.loan.loanType  not equalTo(InvalidLoanTypeExemptCode)) and
      (lar.loan.loanPurpose  not equalTo(InvalidLoanPurposeExemptCode)) and
      (lar.loan.constructionMethod  not equalTo(InvalidConstructionMethodExemptCode)) and
      (lar.loan.occupancy  not equalTo(InvalidOccupancyExemptCode)) and
      (lar.loan.amount  not equalTo(BigDecimal(invalidExemptCode))) and
      (lar.loan.rateSpread  not equalTo(invalidExemptCode.toString)) and
      (lar.action.preapproval  not equalTo(InvalidPreapprovalExemptCode)) and
      (lar.action.actionTakenType  not equalTo(InvalidActionTakenTypeExemptCode)) and
      (lar.applicant.ethnicity.ethnicity1  not equalTo(InvalidEthnicityExemptCode)) and
      (lar.applicant.ethnicity.ethnicity2  not equalTo(InvalidEthnicityExemptCode)) and
      (lar.applicant.ethnicity.ethnicity3  not equalTo(InvalidEthnicityExemptCode)) and
      (lar.applicant.ethnicity.ethnicity4  not equalTo(InvalidEthnicityExemptCode)) and
      (lar.applicant.ethnicity.ethnicity5  not equalTo(InvalidEthnicityExemptCode))  and
      (lar.applicant.ethnicity.ethnicityObserved  not equalTo(InvalidEthnicityObservedExemptCode))  and
      (lar.coApplicant.ethnicity.ethnicityObserved  not equalTo(InvalidEthnicityObservedExemptCode)) and
      (lar.applicant.race.race1  not equalTo(InvalidRaceExemptCode)) and
      (lar.applicant.race.race2  not equalTo(InvalidRaceExemptCode)) and
      (lar.applicant.race.race3  not equalTo(InvalidRaceExemptCode)) and
      (lar.applicant.race.race4  not equalTo(InvalidRaceExemptCode)) and
      (lar.applicant.race.race5  not equalTo(InvalidRaceExemptCode))  and
      (lar.coApplicant.race.race1  not equalTo(InvalidRaceExemptCode)) and
      (lar.coApplicant.race.race2  not equalTo(InvalidRaceExemptCode)) and
      (lar.coApplicant.race.race3  not equalTo(InvalidRaceExemptCode)) and
      (lar.coApplicant.race.race4  not equalTo(InvalidRaceExemptCode)) and
      (lar.coApplicant.race.race5  not equalTo(InvalidRaceExemptCode))   and
      (lar.applicant.race.raceObserved  not equalTo(InvalidRaceObservedExemptCode))  and
      (lar.coApplicant.race.raceObserved  not equalTo(InvalidRaceObservedExemptCode)) and
      (lar.applicant.sex.sexEnum  not equalTo(InvalidSexExemptCode))  and
      (lar.coApplicant.sex.sexEnum  not equalTo(InvalidSexExemptCode)) and
      (lar.applicant.sex.sexObservedEnum  not equalTo(InvalidSexObservedExemptCode))  and
      (lar.coApplicant.sex.sexObservedEnum  not equalTo(InvalidSexObservedExemptCode)) and
      (lar.applicant.age not equalTo(invalidExemptCode)) and
      (lar.coApplicant.age not equalTo(invalidExemptCode)) and
      (lar.AUS.aus2 not equalTo(AUSExempt)) and
      (lar.AUS.aus3 not equalTo(AUSExempt)) and
      (lar.AUS.aus4 not equalTo(AUSExempt)) and
      (lar.AUS.aus5 not equalTo(AUSExempt)) and
      (lar.ausResult.ausResult2 not equalTo(AUSResultExempt)) and
      (lar.ausResult.ausResult3 not equalTo(AUSResultExempt)) and
      (lar.ausResult.ausResult4 not equalTo(AUSResultExempt)) and
      (lar.ausResult.ausResult5 not equalTo(AUSResultExempt))

  }
}