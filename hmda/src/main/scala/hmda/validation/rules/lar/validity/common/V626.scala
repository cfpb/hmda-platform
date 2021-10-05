package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.model.census.Census

object V626 {
  def withIndexedCounties(indexedCounties: Map[String, Census]): EditCheck[LoanApplicationRegister] =
    new V626(indexedCounties)
}

class V626 private (indexedCounties: Map[String, Census]) extends EditCheck[LoanApplicationRegister] {
  private val countyKeys: Set[String] = indexedCounties.keys.toSet

  override def name: String = "V626"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.geography.county is equalTo("NA") or
      (
        lar.geography.county.length is equalTo(5) and
          (
            lar.geography.county is containedInSet(countyKeys)
            )
        )
}