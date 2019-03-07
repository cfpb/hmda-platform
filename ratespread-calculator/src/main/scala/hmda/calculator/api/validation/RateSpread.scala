package hmda.calculator.api.validation

object RateSpread {

  def rateSpread(loanId: String): String = {
    loanId
  }

  def validateRateSpread(rs: String): Boolean = {
    true
  }
}
