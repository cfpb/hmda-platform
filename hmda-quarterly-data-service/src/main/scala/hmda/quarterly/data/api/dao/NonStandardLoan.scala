package hmda.quarterly.data.api.dao

import slick.jdbc.GetResult

import java.sql.Timestamp

object NonStandardLoanType extends Enumeration {
  type NonStandardLoanType = NonStandardLoanTypeVal
  val BALLOON_PAYMENT: NonStandardLoanTypeVal = Value("baloon_payment", "Balloon Payment")
  val REVERSE_MORTGAGE: NonStandardLoanTypeVal = Value("reverse_mortgage", "Reverse Mortgage")
  val BUSINESS_OR_COMMERCIAL: NonStandardLoanTypeVal = Value("business_or_commercial", "Business or Commercial")
  val INTEREST_ONLY_PAYMENT: NonStandardLoanTypeVal = Value("insert_only_payment", "Interest Only Payment")
  val AMORTIZATION: NonStandardLoanTypeVal = Value("amortization", "Negative Amortization")

  case class NonStandardLoanTypeVal(name: String, description: String) extends Val(nextId, name)
  protected final def Value(name: String, description: String) = NonStandardLoanTypeVal(name, description)
}

case class NonStandardLoanVolume(
  lastUpdated: Timestamp,
  quarter: String,
  loanType: NonStandardLoanType.Value,
  volume: Float) extends AggregatedVolume
object NonStandardLoanVolume {
  implicit val getResults: GetResult[NonStandardLoanVolume] = GetResult(result => NonStandardLoanVolume(
    result.rs.getTimestamp("last_updated"),
    result.rs.getString("quarter"),
    NonStandardLoanType.withName(result.rs.getString("loan_type")),
    result.rs.getFloat("volume")
  ))
}
