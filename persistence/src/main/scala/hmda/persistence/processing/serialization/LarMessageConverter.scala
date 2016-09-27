package hmda.persistence.processing.serialization

import hmda.fi.model._
import hmda.model.fi.lar._

trait LarMessageConverter {
  def messageToLoanApplicationRegister(lar: LoanApplicationRegisterMessage): LoanApplicationRegister = {
    val id = lar.id
    val respondentId = lar.respondentId
    val agencyCode = lar.agencyCode
    val loan = messageToLoan(lar.loan).getOrElse(Loan())
    val preapprovals = lar.preapprovals
    val actionTakenType = lar.actionTakenType
    val actionTakenDate = lar.actionTakenDate
    val geography = messageToGeography(lar.geography).getOrElse(Geography())
    val applicant = messageToApplicant(lar.applicant).getOrElse(Applicant())
    val purchaserType = lar.purchaserType
    val denial = messageToDenial(lar.denial).getOrElse(Denial())
    val rateSpread = lar.rateSpread
    val hoepaStatus = lar.hoepaStatus
    val lienStatus = lar.lienStatus
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

  def loanApplicationRegisterToMessage(lar: LoanApplicationRegister): Option[LoanApplicationRegisterMessage] = {
    val id = lar.id
    val respondentId = lar.respondentId
    val agencyCode = lar.agencyCode
    val loan = loanToMessage(lar.loan)
    val preapprovals = lar.preapprovals
    val actionTakenType = lar.actionTakenType
    val actionTakenDate = lar.actionTakenDate
    val geography = geographyToMessage(lar.geography)
    val applicant = applicantToMessage(lar.applicant)
    val purchaserType = lar.purchaserType
    val denial = denialToMessage(lar.denial)
    val rateSpread = lar.rateSpread
    val hoepaStatus = lar.hoepaStatus
    val lienStatus = lar.lienStatus
    Some(
      LoanApplicationRegisterMessage(
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
    )
  }

  def messageToLoan(loan: Option[LoanMessage]): Option[Loan] = {
    loan.map { l =>
      Loan(
        l.id,
        l.applicationDate,
        l.loanType,
        l.propertyType,
        l.purpose,
        l.occupancy,
        l.amount
      )
    }
  }

  def loanToMessage(loan: Loan): Option[LoanMessage] = {
    Some(
      LoanMessage(
        loan.id,
        loan.applicationDate,
        loan.loanType,
        loan.propertyType,
        loan.purpose,
        loan.occupancy,
        loan.amount
      )
    )
  }

  def messageToGeography(geography: Option[GeographyMessage]): Option[Geography] = {
    geography.map { g =>
      Geography(g.msa, g.state, g.county, g.tract)
    }
  }

  def geographyToMessage(geography: Geography): Option[GeographyMessage] = {
    Some(
      GeographyMessage(
        geography.msa,
        geography.state,
        geography.county,
        geography.tract
      )
    )
  }

  def messageToApplicant(applicant: Option[ApplicantMessage]): Option[Applicant] = {
    applicant.map { a =>
      Applicant(
        a.ethnicity,
        a.coEthnicity,
        a.race1,
        a.race2,
        a.race3,
        a.race4,
        a.race5,
        a.coRace1,
        a.coRace2,
        a.coRace3,
        a.coRace4,
        a.coRace5,
        a.sex,
        a.coSex,
        a.income
      )
    }
  }

  def applicantToMessage(applicant: Applicant): Option[ApplicantMessage] = {
    Some(
      ApplicantMessage(
        applicant.ethnicity,
        applicant.coEthnicity,
        applicant.race1,
        applicant.race2,
        applicant.race3,
        applicant.race4,
        applicant.race5,
        applicant.coRace1,
        applicant.coRace2,
        applicant.coRace3,
        applicant.coRace4,
        applicant.coRace5,
        applicant.sex,
        applicant.coSex,
        applicant.income
      )
    )
  }

  def messageToDenial(denial: Option[DenialMessage]): Option[Denial] = {
    denial.map { d =>
      Denial(d.reason1, d.reason2, d.reason3)
    }
  }

  def denialToMessage(denial: Denial): Option[DenialMessage] = {
    Some(
      DenialMessage(
        denial.reason1,
        denial.reason2,
        denial.reason3
      )
    )
  }
}
