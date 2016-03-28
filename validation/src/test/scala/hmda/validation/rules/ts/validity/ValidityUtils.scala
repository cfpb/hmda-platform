package hmda.validation.rules.ts.validity

import hmda.model.fi.ts.Respondent

trait ValidityUtils {
  def respondentNotEmpty(r: Respondent): Boolean = {
    r.id != "" &&
      r.name != "" &&
      r.address != "" &&
      r.city != "" &&
      r.state != "" &&
      r.zipCode != ""
  }

  def respondentIsEmpty(r: Respondent): Boolean = {
    r.id == "" &&
      r.name == "" &&
      r.address == "" &&
      r.city == "" &&
      r.state == "" &&
      r.zipCode == ""
  }

}
