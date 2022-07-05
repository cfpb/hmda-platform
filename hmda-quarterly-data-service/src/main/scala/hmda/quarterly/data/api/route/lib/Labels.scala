package hmda.quarterly.data.api.route.lib

import com.typesafe.config.ConfigFactory

object Labels {
  private val config = ConfigFactory.load().getConfig("graph.value_labels")
  val APPS: String = config.getString("apps")
  val LOANS: String = config.getString("loans")
  val CREDIT_SCORES: String = config.getString("credit_scores")
  val CLTV: String = config.getString("cltv")
  val DTI: String = config.getString("dti")
  val DENIALS: String = config.getString("denials")
  val INTEREST_RATES: String = config.getString("interest_rates")
  val TLC: String = config.getString("tlc")
}
