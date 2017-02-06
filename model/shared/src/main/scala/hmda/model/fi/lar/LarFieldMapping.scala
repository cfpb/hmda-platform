package hmda.model.fi.lar

// Maps a field's friendly name to its value in the LAR record
object LarFieldMapping {
  def mapping(lar: LoanApplicationRegister): Map[String, Any] = Map(
    "Record Identifier" -> lar.id,
    "Respondent Id" -> lar.respondentId,
    "Agency Code" -> lar.agencyCode,
    "Loan Id" -> lar.loan.id,
    "Loan Application Date" -> lar.loan.applicationDate,
    "Date Application Received" -> lar.loan.applicationDate,
    "Loan Type" -> lar.loan.loanType,
    "Property Type" -> lar.loan.propertyType,
    "Loan Purpose" -> lar.loan.purpose,
    "Owner Occupancy" -> lar.loan.occupancy,
    "Loan Amount" -> lar.loan.amount,
    "Preapprovals" -> lar.preapprovals,
    "Type of Action Taken" -> lar.actionTakenType,
    "Date of Action" -> lar.actionTakenDate,
    "Metropolitan Statistical Area / Metropolitan Division" -> lar.geography.msa,
    "State Code" -> lar.geography.state,
    "County Code" -> lar.geography.county,
    "Census Tract" -> lar.geography.tract,
    "Applicant Ethnicity" -> lar.applicant.ethnicity,
    "Co-applicant Ethnicity" -> lar.applicant.coEthnicity,
    "Applicant Race: 1" -> lar.applicant.race1,
    "Applicant Race: 2" -> lar.applicant.race2,
    "Applicant Race: 3" -> lar.applicant.race3,
    "Applicant Race: 4" -> lar.applicant.race4,
    "Applicant Race: 5" -> lar.applicant.race5,
    "Co-applicant Race: 1" -> lar.applicant.coRace1,
    "Co-applicant Race: 2" -> lar.applicant.coRace2,
    "Co-applicant Race: 3" -> lar.applicant.coRace3,
    "Co-applicant Race: 4" -> lar.applicant.coRace4,
    "Co-applicant Race: 5" -> lar.applicant.coRace5,
    "Applicant Sex" -> lar.applicant.sex,
    "Co-applicant Sex" -> lar.applicant.coSex,
    "Applicant Income" -> lar.applicant.income,
    "Type of Purchaser" -> lar.purchaserType,
    "Denial Reason: 1" -> lar.denial.reason1,
    "Denial Reason: 2" -> lar.denial.reason2,
    "Denial Reason: 3" -> lar.denial.reason3,
    "Rate Spread" -> lar.rateSpread,
    "HOEPA Status" -> lar.hoepaStatus,
    "Lien Status" -> lar.lienStatus
  )

}
