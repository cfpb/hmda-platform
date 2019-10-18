package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.model.census.Census

import scala.util.Try

object V627 {
  def withIndexedCounties(indexedCounties: Map[String, Census]): EditCheck[LoanApplicationRegister] =
    new V627(indexedCounties)
}

class V627 private (indexedCounties: Map[String, Census]) extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V627"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.geography.county not equalTo("NA") and (lar.geography.tract not equalTo("NA"))) {
      val firstFive = Try(lar.geography.tract.substring(0, 5)).getOrElse("")
      firstFive is equalTo(lar.geography.county) and
        (firstFive is containedIn(indexedCounties.keys.toList))
    }
}
