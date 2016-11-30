package hmda.validation.engine.lar.`macro`

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.api.ValidationApi
import hmda.validation.engine.lar.LarCommonEngine
import hmda.validation.engine.{ Macro, ValidationErrorType }
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource
import hmda.validation.rules.lar.`macro`._

import scala.concurrent.{ ExecutionContext, Future }
import scalaz.Alpha.Q

trait LarMacroEngine extends LarCommonEngine with ValidationApi {

  def checkMacro(larSource: LoanApplicationRegisterSource)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Future[LarSourceValidation] = {
    Future.sequence(
      List(
        Q007,
        Q008,
        Q009,
        Q010,
        Q016,
        Q023,
        Q047,
        Q055,
        Q056,
        Q057,
        Q058,
        Q061,
        Q073,
        Q074,
        Q080,
        Q081,
        Q082,
        Q083
      ).map(checkAggregate(_, larSource, "", Macro))
    )
      .map(checks => validateAll(checks, larSource))

  }

  private def checkAggregate(
    editCheck: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister],
    input: LoanApplicationRegisterSource,
    inputId: String,
    errorType: ValidationErrorType
  )(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Future[LarSourceValidation] = {
    val fResult = editCheck(input)
    for {
      result <- fResult
    } yield {
      convertResult(input, result, editCheck.name, inputId, errorType)
    }
  }

}
