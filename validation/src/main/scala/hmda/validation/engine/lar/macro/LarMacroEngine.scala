package hmda.validation.engine.lar.`macro`

import hmda.validation._
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.validation.{Macro, Syntactical, ValidationErrorType}
import hmda.validation.api.ValidationApi
import hmda.validation.context.ValidationContext
import hmda.validation.engine.lar.LarCommonEngine
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource
import hmda.validation.rules.lar.`macro`._
import hmda.validation.rules.lar.syntactical.S040

import scala.concurrent.Future

trait LarMacroEngine extends LarCommonEngine with ValidationApi {

  def checkMacro[_: AS: MAT: EC](larSource: LoanApplicationRegisterSource, ctx: ValidationContext): Future[LarSourceValidation] = {
    Future.sequence(
      List(
        Q006,
        Q007,
        Q008,
        Q009,
        Q010,
        Q011.inContext(ctx),
        Q015,
        Q016,
        Q023,
        Q031,
        Q047,
        Q048,
        Q053,
        Q054,
        Q055,
        Q056,
        Q057,
        Q058,
        Q061,
        Q062,
        Q063,
        Q065,
        Q070.inContext(ctx),
        Q071.inContext(ctx),
        Q072.inContext(ctx),
        Q073,
        Q074,
        Q075.inContext(ctx),
        Q076.inContext(ctx),
        Q080,
        Q081,
        Q082,
        Q083
      ).map(checkAsync(_, larSource, "", Macro, false))
    )
      .map(checks => validateAll(checks, larSource))

  }

<<<<<<< HEAD
=======
  def checkS040[_: AS: MAT: EC](larSource: LoanApplicationRegisterSource, ctx: ValidationContext): Future[LarSourceValidation] = {
    val fCheck = checkAggregate(S040, larSource, "", Syntactical)
    fCheck.map(checks => validateAll(List(checks), larSource))
  }

  private def checkAggregate[_: AS: MAT: EC](
    editCheck: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister],
    input: LoanApplicationRegisterSource,
    inputId: String,
    errorType: ValidationErrorType
  ): Future[LarSourceValidation] = {
    val fResult = editCheck(input)
    for {
      result <- fResult
    } yield {
      convertResult(input, result, editCheck.name, inputId, errorType, false)
    }
  }

>>>>>>> bb73a2f9... RESET ME: start implementing s040 as macro
}
