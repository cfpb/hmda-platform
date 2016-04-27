package hmda.validation.rules.lar.validity

import hmda.model.census.{ CBSATract, CBSATractLookup }

trait CensusEditCheck {

  val cbsaTracts = CBSATractLookup.values

  def msaCode(code: String): String = {
    val md = cbsaTracts.filter(m => m.metDivFp == code)
    if (md.nonEmpty) {
      md.head.geoidMsa
    } else {
      code
    }
  }
}
