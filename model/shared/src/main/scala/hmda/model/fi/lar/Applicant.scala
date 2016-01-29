package hmda.model.fi.lar

case class Applicant(
  ethnicity: Int,
  coEthnicity: Int,
  race1: Int,
  race2: String,
  race3: String,
  race4: String,
  race5: String,
  coRace1: Int,
  coRace2: String,
  coRace3: String,
  coRace4: String,
  coRace5: String,
  sex: Int,
  coSex: Int,
  income: String
)

