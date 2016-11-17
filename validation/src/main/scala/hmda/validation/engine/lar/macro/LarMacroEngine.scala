package hmda.validation.engine.lar.`macro`

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.api.ValidationApi
import hmda.validation.engine.lar.LarCommonEngine
import hmda.validation.engine.{Macro, ValidationErrorType}
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource
<<<<<<< HEAD
import hmda.validation.rules.lar.`macro`.{Q007, Q008, Q047, Q056}
=======
import hmda.validation.rules.lar.`macro`._
>>>>>>> cfbff1ec35032561f4d149bc48c9291a5600fd10

import scala.concurrent.{ExecutionContext, Future}

trait LarMacroEngine extends LarCommonEngine with ValidationApi {

  def checkMacro(larSource: LoanApplicationRegisterSource)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Future[LarSourceValidation] = {
    Future.sequence(
      List(
        Q007,
        Q008,
        Q047,
        Q056,
        Q057,
        Q058
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
