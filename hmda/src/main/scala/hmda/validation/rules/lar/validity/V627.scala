package hmda.validation.rules.lar.validity

import hmda.census.records.CensusRecords
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

import scala.util.Try

object V627 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V627"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    when(
      lar.geography.county not equalTo("NA") and (lar.geography.tract not equalTo(
        "NA"))) {
      val firstFive = Try(lar.geography.tract.substring(0, 5)).getOrElse("")
      firstFive is equalTo(lar.geography.county) and
        (firstFive is containedIn(CensusRecords.indexedCounty.keys.toList))
    }
  }
}
