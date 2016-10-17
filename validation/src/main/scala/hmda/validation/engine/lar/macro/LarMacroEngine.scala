package hmda.validation.engine.lar.`macro`

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.api.ValidationApi
import hmda.validation.engine.{ Macro, ValidationErrorType }
import hmda.validation.engine.lar.LarCommonEngine
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource
import hmda.validation.rules.lar.`macro`.{ Q007, Q008 }

import scala.concurrent.{ ExecutionContext, Future }

trait LarMacroEngine extends LarCommonEngine with ValidationApi {

  implicit val system: ActorSystem = ActorSystem("macro-edits-system")

  implicit val ec: ExecutionContext = system.dispatcher

  implicit val materializer: ActorMaterializer = ActorMaterializer()

  def checkMacro(larSource: LoanApplicationRegisterSource): Future[LarSourceValidation] = {
    Future.sequence(
      List(
        Q007,
        Q008
      ).map(checkAggregate(_, larSource, "", Macro))
    )
      .map(checks => validateAll(checks, larSource))

  }

  private def checkAggregate(
    editCheck: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister],
    input: LoanApplicationRegisterSource,
    inputId: String,
    errorType: ValidationErrorType
  )(implicit ec: ExecutionContext): Future[LarSourceValidation] = {
    val fResult = editCheck(input)
    for {
      result <- fResult
    } yield {
      convertResult(input, result, editCheck.name, inputId, errorType)
    }
  }

}
