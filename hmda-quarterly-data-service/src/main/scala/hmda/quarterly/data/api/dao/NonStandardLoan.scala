package hmda.quarterly.data.api.dao

import slick.jdbc.GetResult

import java.sql.Timestamp

object NonStandardLoanType extends Enumeration {
  type NonStandardLoanType = Value
  val BALLOON_PAYMENT: Value = Value("baloon_payment", "Balloon Payment")
  val REVERSE_MORTGAGE: Value = Value("reverse_mortgage", "Reverse Mortgage")
  val BUSINESS_OR_COMMERCIAL: Value = Value("business_or_commercial", "Business or Commercial")
  val INTEREST_ONLY_PAYMENT: Value = Value("insert_only_payment", "Interest Only Payment")
  val AMORTIZATION: Value = Value("amortization", "Negative Amortization")

  case class NonStandardLoanTypeVal(name: String, description: String) extends Val(nextId, name)
  protected final def Value(name: String, description: String) = new NonStandardLoanTypeVal(name, description)
}

import NonStandardLoanType._
case class NonStandardLoanVolume(
  lastUpdated: Timestamp,
  quarter: String,
  loanType: NonStandardLoanType,
  volume: Long) extends AggregatedVolume
object NonStandardLoanVolume {
  implicit val getResults: GetResult[NonStandardLoanVolume] = GetResult(result => NonStandardLoanVolume(
    result.rs.getTimestamp("last_updated"),
    result.rs.getString("quarter"),
    NonStandardLoanType.withName(result.rs.getString("loan_type")),
    result.rs.getLong("volume")
  ))
}
