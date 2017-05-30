package hmda.validation.engine.lar.syntactical

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.validation.Syntactical
import hmda.validation.api.ValidationApi
import hmda.validation.context.ValidationContext
import hmda.validation.engine.lar.LarCommonEngine
import hmda.validation.rules.ts.syntactical.{ S020, S025 }
import hmda.validation.rules.lar.syntactical._

trait LarSyntacticalEngine extends LarCommonEngine with ValidationApi {

  def checkSyntactical(lar: LoanApplicationRegister, ctx: ValidationContext): LarValidation = {
    val checksToRun = List(
      S010,
      S020,
      S025.inContext(ctx),
      S205,
      S270.inContext(ctx)
    )
    val checks = checksToRun.map(check(_, lar, lar.loan.id, Syntactical, false))

    validateAll(checks, lar)
  }

  def checkSyntacticalCollection(lars: Iterable[LoanApplicationRegister]): LarsValidation = {
    val checks = List(
      S011,
      S040
    ).map(check(_, lars, "", Syntactical, false))

    validateAll(checks, lars)
  }

}
