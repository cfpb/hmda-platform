package hmda.validation.rules.lar.quality

import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object Q045 extends EditCheck[LoanApplicationRegister] {
  override def apply(lar: LoanApplicationRegister): Result = {

    val config = ConfigFactory.load()
    val rateSpread = config.getDouble("hmda.validation.quality.Q045.rateSpread")

    when(lar.rateSpread is numeric) {
      when(
        (lar.actionTakenType is equalTo(1)) and
          (lar.lienStatus is equalTo(2)) and
          (lar.rateSpread.toDouble is greaterThan(rateSpread))
      ) {
          lar.hoepaStatus is equalTo(1)
        }
    }
  }

  override def name = "Q045"

  override def fields(lar: LoanApplicationRegister) = Map(
    noField -> ""
  )

}
