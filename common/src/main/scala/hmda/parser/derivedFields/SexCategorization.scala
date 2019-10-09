package hmda.parser.derivedFields

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._

package object SexCategorization {

  def assignSexCategorization(lar: LoanApplicationRegister): String = {
    val sex   = lar.applicant.sex.sexEnum
    val coSex = lar.coApplicant.sex.sexEnum

    if (sex == SexInformationNotProvided || sex == SexNotApplicable) {
      "Sex Not Available"
    } else if (sex == Male && (coSex != Female && coSex != MaleAndFemale)) {
      Male.description
    } else if (sex == Female && (coSex != Male && coSex != MaleAndFemale)) {
      Female.description
    } else if (sex == Male && (coSex == Female || coSex == MaleAndFemale)) {
      "Joint"
    } else if (sex == Female && (coSex == Male || coSex == MaleAndFemale)) {
      "Joint"
    } else if (sex == MaleAndFemale) {
      "Joint"
    } else {
      "Sex Not Available"
    }
  }
}
