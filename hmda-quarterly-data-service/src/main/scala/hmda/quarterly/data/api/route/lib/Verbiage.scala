package hmda.quarterly.data.api.route.lib

import com.typesafe.config.ConfigFactory

object Verbiage {
  final val config = ConfigFactory.load().getConfig("graph.verbiage")
  final val SUMMARY = config.getString("summary")
  final val DEFAULT_DECIMAL_PRECISION = config.getInt("decimal_precision")
  final val INTEREST_DECIMAL_PRECISION = config.getInt("interest_decimal_precision")
  final val COUNT_DECIMAL_PRECISION = config.getInt("count_decimal_precision")
  final object Race {
    final val ASIAN = config.getString("race.asian")
    final val BLACK = config.getString("race.black")
    final val HISPANIC = config.getString("race.hispanic")
    final val WHITE = config.getString("race.white")
  }
  final object LoanType {
    final val CONVENTIONAL_CONFORMING = config.getString("loan_type.conventional_conforming")
    final val CONVENTIONAL_NON_CONFORMING = config.getString("loan_type.conventional_non_conforming")
    final val FHA = config.getString("loan_type.fha")
    final val HELOC = config.getString("loan_type.heloc")
    final val RHS_FSA = config.getString("loan_type.rhs_fsa")
    final val VA = config.getString("loan_type.va")
  }
}
