package hmda.model.fi.lar

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

@ScalaJSDefined
trait LoanApplicationRegisterJS extends js.Object {
  val id: Int
  val respondentId: String
  val agencyCode: Int
  val loan: LoanJS
  val preapprovals: Int
  val actionTakenType: Int
  val actionTakenDate: Int
  val geography: GeographyJS
  val applicant: ApplicantJS
  val purchaserType: Int
  val denial: DenialJS
  val rateSpread: String
  val hoepaStatus: Int
  val lienStatus: Int
}

@ScalaJSDefined
trait GeographyJS extends js.Object {
  val msa: String
  val state: String
  val county: String
  val tract: String
}

@ScalaJSDefined
trait DenialJS extends js.Object {
  val reason1: String
  val reason2: String
  val reason3: String
}

@ScalaJSDefined
trait LoanJS extends js.Object {
  val id: String
  val applicationDate: String
  val loanType: Int
  val propertyType: Int
  val purpose: Int
  val occupancy: Int
  val amount: Int
}

@ScalaJSDefined
trait ApplicantJS extends js.Object {
  val ethnicity: Int
  val coEthnicity: Int
  val race1: Int
  val race2: String
  val race3: String
  val race4: String
  val race5: String
  val coRace1: Int
  val coRace2: String
  val coRace3: String
  val coRace4: String
  val coRace5: String
  val sex: Int
  val coSex: Int
  val income: String
}
