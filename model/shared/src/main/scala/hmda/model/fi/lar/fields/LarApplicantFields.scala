package hmda.model.fi.lar.fields

import hmda.model.fi.RecordField

object LarApplicantFields {

  val ethnicity = RecordField("Applicant Ethnicity", "")

  val coEthnicity = RecordField("Co-applicant Ethnicity", "")

  val race1 = RecordField("Applicant Race: 1", "")

  val race2 = RecordField("Applicant Race: 2", "")

  val race3 = RecordField("Applicant Race: 3", "")

  val race4 = RecordField("Applicant Race: 4", "")

  val race5 = RecordField("Applicant Race: 5", "")

  val coRace1 = RecordField("Co-applicant Race: 1", "")

  val coRace2 = RecordField("Co-applicant Race: 2", "")

  val coRace3 = RecordField("Co-applicant Race: 3", "")

  val coRace4 = RecordField("Co-applicant Race: 4", "")

  val coRace5 = RecordField("Co-applicant Race: 5", "")

  val sex = RecordField("Applicant Sex", "")

  val coSex = RecordField("Co-applicant Sex", "")

  val income = RecordField("Applicant Income", "")

}
