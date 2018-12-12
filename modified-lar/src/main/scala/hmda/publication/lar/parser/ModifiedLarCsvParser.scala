package hmda.publication.lar.parser

import hmda.publication.lar.model.ModifiedLoanApplicationRegister

object ModifiedLarCsvParser {

  //TODO: Parse LAR CSV and convert to Modified LAR
  def apply(s: String): ModifiedLoanApplicationRegister =
    ModifiedLoanApplicationRegister()
}
