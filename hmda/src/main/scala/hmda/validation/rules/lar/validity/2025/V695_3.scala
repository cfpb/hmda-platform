package hmda.validation.rules.lar.validity._2025

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.parser.filing.ts.TsCsvParser.toValidBigInt
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

import scala.util.Try

object V695_3 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V695-3"

  override def parent: String = "V695"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {

    val nmlsrLen = lar.larIdentifier.NMLSRIdentifier.length
    val nmlsrID = lar.larIdentifier.NMLSRIdentifier
  when (nmlsrID not oneOf("Exempt", "NA")  ){

    (nmlsrLen is lessThanOrEqual(12)) and
      (nmlsrLen is greaterThanOrEqual(4))  and
      (toValidBigInt(nmlsrID) is greaterThan(0)) and
      (nmlsrID not double)
  }
  }

}
