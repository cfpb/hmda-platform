package hmda.validation.rules.lar.quality._2021

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax.PredicateOps
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.validity._2020.V696_2.{ausOtherList, invalidAUSResult}

import scala.util.Try

object Q657 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q657"

  override def apply(lar: LoanApplicationRegister): ValidationResult ={
    //check proeprty fields
    (lar.property.totalUnits not equalTo(1111)) and

    //check loan fields
    (lar.loan.loanType  not equalTo(InvalidLoanTypeExemptCode)) and
      (lar.loan.loanPurpose  not equalTo(InvalidLoanPurposeExemptCode)) and
      (lar.loan.constructionMethod  not equalTo(InvalidConstructionMethodExemptCode)) and
      (lar.loan.occupancy  not equalTo(InvalidOccupancyExemptCode)) and

    //check action fields
      (lar.action.preapproval  not equalTo(InvalidPreapprovalExemptCode)) and
       (lar.action.actionTakenType  not equalTo(InvalidActionTakenTypeExemptCode)) and


      (lar.applicant.ethnicity.ethnicity1.code  not equalTo(1111)) and
      (lar.applicant.ethnicity.ethnicity2.code  not equalTo(1111)) and
      (lar.applicant.ethnicity.ethnicity3.code  not equalTo(1111)) and
      (lar.applicant.ethnicity.ethnicity4.code  not equalTo(1111)) and
      (lar.applicant.ethnicity.ethnicity5.code  not equalTo(1111))  and
      (lar.applicant.ethnicity.ethnicityObserved.code  not equalTo(1111))  and
      (lar.coApplicant.ethnicity.ethnicityObserved.code  not equalTo(1111)) and
      (lar.applicant.race.race1.code  not equalTo(1111)) and
      (lar.applicant.race.race2.code  not equalTo(1111)) and
      (lar.applicant.race.race3.code  not equalTo(1111)) and
      (lar.applicant.race.race4.code  not equalTo(1111)) and
      (lar.applicant.race.race5.code  not equalTo(1111))  and
      (lar.coApplicant.race.race1.code  not equalTo(1111)) and
      (lar.coApplicant.race.race2.code  not equalTo(1111)) and
      (lar.coApplicant.race.race3.code  not equalTo(1111)) and
      (lar.coApplicant.race.race4.code  not equalTo(1111)) and
      (lar.coApplicant.race.race5.code  not equalTo(1111))   and
      (lar.applicant.race.raceObserved.code  not equalTo(1111))  and
      (lar.coApplicant.race.raceObserved.code  not equalTo(1111)) and
      (lar.applicant.sex.sexEnum.code  not equalTo(1111))  and
      (lar.coApplicant.sex.sexEnum.code  not equalTo(1111)) and
      (lar.applicant.sex.sexObservedEnum.code  not equalTo(1111))  and
      (lar.coApplicant.sex.sexObservedEnum.code  not equalTo(1111)) and
        (lar.applicant.age not equalTo(1111)) and
        (lar.coApplicant.age not equalTo(1111)) and
       (lar.purchaserType.code not equalTo(1111)) and
      (lar.hoepaStatus.code not equalTo(1111)) and
      (lar.lienStatus.code not equalTo(1111)) and
      (lar.AUS.aus2.code not equalTo(1111)) and
      (lar.AUS.aus3.code not equalTo(1111)) and
      (lar.AUS.aus4.code not equalTo(1111)) and
      (lar.AUS.aus5.code not equalTo(1111)) and
      (lar.ausResult.ausResult2.code not equalTo(1111)) and
      (lar.ausResult.ausResult3.code not equalTo(1111)) and
      (lar.ausResult.ausResult4.code not equalTo(1111)) and
      (lar.ausResult.ausResult4.code not equalTo(1111))
  }
}