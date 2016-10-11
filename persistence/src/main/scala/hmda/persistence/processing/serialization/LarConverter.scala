package hmda.persistence.processing.serialization

import hmda.fi.lar.LoanApplicationRegisterMessage
import hmda.model.fi.lar._

import scala.language.implicitConversions

object LarConverter {

  implicit def optionMessageToLoanApplicationRegister(o: Option[LoanApplicationRegisterMessage]): LoanApplicationRegister = {
    o.map { m =>
      messageToLoanApplicationRegister(m)
    }.getOrElse(
      LoanApplicationRegister(
        0,
        "",
        0,
        Loan(),
        0,
        0,
        0,
        Geography(),
        Applicant(),
        0,
        Denial(),
        "",
        0,
        0
      )
    )
  }

  implicit def messageToLoanApplicationRegister(m: LoanApplicationRegisterMessage): LoanApplicationRegister = {
    val id = m.id
    val respondentId = m.respondentId
    val agencyCode = m.agencyCode
    val loanId = m.loanId
    val applicationDate = m.applicationDate
    val loanType = m.loanType
    val propertyType = m.propertyType
    val loanPurpose = m.loanPurpose
    val occupancy = m.occupancy
    val loanAmmount = m.loanAmount
    val preapprovals = m.preapprovals
    val actionTakenType = m.actionTakenType
    val actionTakenDate = m.actionTakenDate
    val msa = m.msa
    val state = m.state
    val county = m.county
    val tract = m.tract
    val ethnicity = m.ethnicity
    val coEthnicity = m.coEthnicity
    val race1 = m.race1
    val race2 = m.race2
    val race3 = m.race3
    val race4 = m.race4
    val race5 = m.race5
    val coRace1 = m.coRace1
    val coRace2 = m.coRace2
    val coRace3 = m.coRace3
    val coRace4 = m.coRace4
    val coRace5 = m.coRace5
    val sex = m.sex
    val coSex = m.coSex
    val income = m.income
    val purchaserType = m.purchaserType
    val reason1 = m.reason1
    val reason2 = m.reason2
    val reason3 = m.reason3
    val rateSpread = m.rateSpread
    val hoepaStatus = m.hoepaStatus
    val lienStatus = m.lienStatus

    val loan = Loan(
      loanId,
      applicationDate,
      loanType,
      propertyType,
      loanPurpose,
      occupancy,
      loanAmmount
    )

    val geography = Geography(
      msa,
      state,
      county,
      tract
    )

    val applicant = Applicant(
      ethnicity,
      coEthnicity,
      race1,
      race2,
      race3,
      race4,
      race5,
      coRace1,
      coRace2,
      coRace3,
      coRace4,
      coRace5,
      sex,
      coSex,
      income
    )

    val denial = Denial(
      reason1,
      reason2,
      reason3
    )

    LoanApplicationRegister(
      id,
      respondentId,
      agencyCode,
      loan,
      preapprovals,
      actionTakenType,
      actionTakenDate,
      geography,
      applicant,
      purchaserType,
      denial,
      rateSpread,
      hoepaStatus,
      lienStatus
    )
  }

  implicit def loanApplicationRegisterToMessage(m: LoanApplicationRegister): Option[LoanApplicationRegisterMessage] = {
    val id = m.id
    val respondentId = m.respondentId
    val agencyCode = m.agencyCode
    val loanId = m.loan.id
    val applicationDate = m.loan.applicationDate
    val loanType = m.loan.loanType
    val propertyType = m.loan.propertyType
    val loanPurpose = m.loan.purpose
    val occupancy = m.loan.occupancy
    val loanAmmount = m.loan.amount
    val preapprovals = m.preapprovals
    val actionTakenType = m.actionTakenType
    val actionTakenDate = m.actionTakenDate
    val msa = m.geography.msa
    val state = m.geography.state
    val county = m.geography.county
    val tract = m.geography.tract
    val ethnicity = m.applicant.ethnicity
    val coEthnicity = m.applicant.coEthnicity
    val race1 = m.applicant.race1
    val race2 = m.applicant.race2
    val race3 = m.applicant.race3
    val race4 = m.applicant.race4
    val race5 = m.applicant.race5
    val coRace1 = m.applicant.coRace1
    val coRace2 = m.applicant.coRace2
    val coRace3 = m.applicant.coRace3
    val coRace4 = m.applicant.coRace4
    val coRace5 = m.applicant.coRace5
    val sex = m.applicant.sex
    val coSex = m.applicant.coSex
    val income = m.applicant.income
    val purchaserType = m.purchaserType
    val reason1 = m.denial.reason1
    val reason2 = m.denial.reason2
    val reason3 = m.denial.reason3
    val rateSpread = m.rateSpread
    val hoepaStatus = m.hoepaStatus
    val lienStatus = m.lienStatus

    val lar = LoanApplicationRegisterMessage(
      id,
      respondentId,
      agencyCode,
      loanId,
      applicationDate,
      loanType,
      propertyType,
      loanPurpose,
      occupancy,
      loanAmmount,
      preapprovals,
      actionTakenType,
      actionTakenDate,
      msa,
      state,
      county,
      tract,
      ethnicity,
      coEthnicity,
      race1,
      race2,
      race3,
      race4,
      race5,
      coRace1,
      coRace2,
      coRace3,
      coRace4,
      coRace5,
      sex,
      coSex,
      income,
      purchaserType,
      reason1,
      reason2,
      reason3,
      rateSpread,
      hoepaStatus,
      lienStatus
    )

    Some(lar)
  }

}
