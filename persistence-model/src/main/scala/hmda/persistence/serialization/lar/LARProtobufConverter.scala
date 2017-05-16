package hmda.persistence.serialization.lar

import hmda.model.fi.lar._
import hmda.persistence.model.serialization.LoanApplicationRegister._

object LARProtobufConverter {

  def loanApplicationRegisterToProtobuf(obj: LoanApplicationRegister): LoanApplicationRegisterMessage = {
    LoanApplicationRegisterMessage(
      id = obj.id,
      respondentId = obj.respondentId,
      agencyCode = obj.agencyCode,
      loan = Some(loanToProtobuf(obj.loan)),
      preapprovals = obj.preapprovals,
      actionTakenType = obj.actionTakenType,
      actionTakenDate = obj.actionTakenDate,
      geography = Some(geographyToProtobuf(obj.geography)),
      applicant = Some(applicantToProtobuf(obj.applicant)),
      purchaserType = obj.purchaserType,
      denial = Some(denialToProtobuf(obj.denial)),
      rateSpread = obj.rateSpread,
      hoepaStatus = obj.hoepaStatus,
      lienStatus = obj.lienStatus
    )
  }

  def loanApplicationRegisterFromProtobuf(msg: LoanApplicationRegisterMessage): LoanApplicationRegister = {
    LoanApplicationRegister(
      id = msg.id,
      respondentId = msg.respondentId,
      agencyCode = msg.agencyCode,
      loan = loanFromProtobuf(msg.loan.getOrElse(LoanMessage())),
      preapprovals = msg.preapprovals,
      actionTakenType = msg.actionTakenType,
      actionTakenDate = msg.actionTakenDate,
      geography = geographyFromProtobuf(msg.geography.getOrElse(GeographyMessage())),
      applicant = applicantFromProtobuf(msg.applicant.getOrElse(ApplicantMessage())),
      purchaserType = msg.purchaserType,
      denial = denialFromProtobuf(msg.denial.getOrElse(DenialMessage())),
      rateSpread = msg.rateSpread,
      hoepaStatus = msg.hoepaStatus,
      lienStatus = msg.lienStatus
    )
  }

  def loanToProtobuf(obj: Loan): LoanMessage = {
    LoanMessage(
      id = obj.id,
      applicationDate = obj.applicationDate,
      loanType = obj.loanType,
      propertyType = obj.propertyType,
      purpose = obj.purpose,
      occupancy = obj.occupancy,
      amount = obj.amount
    )
  }

  def loanFromProtobuf(msg: LoanMessage): Loan = {
    Loan(
      id = msg.id,
      applicationDate = msg.applicationDate,
      loanType = msg.loanType,
      propertyType = msg.propertyType,
      purpose = msg.purpose,
      occupancy = msg.occupancy,
      amount = msg.amount
    )
  }

  def geographyToProtobuf(obj: Geography): GeographyMessage = {
    GeographyMessage(
      msa = obj.msa,
      state = obj.state,
      county = obj.county,
      tract = obj.tract
    )
  }

  def geographyFromProtobuf(msg: GeographyMessage): Geography = {
    Geography(
      msa = msg.msa,
      state = msg.state,
      county = msg.county,
      tract = msg.tract
    )
  }

  def applicantToProtobuf(obj: Applicant): ApplicantMessage = {
    ApplicantMessage(
      ethnicity = obj.ethnicity,
      coEthnicity = obj.coEthnicity,
      race1 = obj.race1,
      race2 = obj.race2,
      race3 = obj.race3,
      race4 = obj.race4,
      race5 = obj.race5,
      coRace1 = obj.coRace1,
      coRace2 = obj.coRace2,
      coRace3 = obj.coRace3,
      coRace4 = obj.coRace4,
      coRace5 = obj.coRace5,
      sex = obj.sex,
      coSex = obj.coSex,
      income = obj.income
    )
  }

  def applicantFromProtobuf(msg: ApplicantMessage): Applicant = {
    Applicant(
      ethnicity = msg.ethnicity,
      coEthnicity = msg.coEthnicity,
      race1 = msg.race1,
      race2 = msg.race2,
      race3 = msg.race3,
      race4 = msg.race4,
      race5 = msg.race5,
      coRace1 = msg.coRace1,
      coRace2 = msg.coRace2,
      coRace3 = msg.coRace3,
      coRace4 = msg.coRace4,
      coRace5 = msg.coRace5,
      sex = msg.sex,
      coSex = msg.coSex,
      income = msg.income
    )
  }

  def denialToProtobuf(obj: Denial): DenialMessage = {
    DenialMessage(
      reason1 = obj.reason1,
      reason2 = obj.reason2,
      reason3 = obj.reason3
    )
  }

  def denialFromProtobuf(msg: DenialMessage): Denial = {
    Denial(
      reason1 = msg.reason1,
      reason2 = msg.reason2,
      reason3 = msg.reason3
    )
  }

}
