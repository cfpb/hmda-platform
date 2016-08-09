package hmda.validation.engine.lar.syntactical

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.api.ValidationApi
import hmda.validation.context.ValidationContext
import hmda.validation.engine.lar.LarCommonEngine
import hmda.validation.rules.ts.syntactical.S025
import hmda.validation.rules.lar.syntactical._

trait LarSyntacticalEngine extends LarCommonEngine with ValidationApi {

  private def s025(lar: LoanApplicationRegister, ctx: ValidationContext): LarValidation = {
    convertResult(lar, S025(lar, ctx), "S025")
  }

  def checkSyntactical(lar: LoanApplicationRegister, ctx: ValidationContext): LarValidation = {
    val checks = List(
      S010,
      S020,
      S205
    ).map(check(_, lar))

    val allChecks = checks :+ s025(lar, ctx)

    validateAll(allChecks, lar)
  }

  def checkSyntacticalCollection(lars: Iterable[LoanApplicationRegister]): LarsValidation = {
    val checks = List(
      S011,
      S040
    ).map(check(_, lars))

    validateAll(checks, lars)
  }

}
