package hmda.validation.rules.lar.validity

import hmda.model.census.CBSATract

trait CensusEditCheck {
  def msaCode(cbsaTracts: Seq[CBSATract], code: String): String = {
    val md = cbsaTracts.filter(m => m.metdivfp == code)
    if (md.nonEmpty) {
      md.head.geoidMsa
    } else {
      code
    }
  }
}
