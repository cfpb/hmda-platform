package hmda.validation.rules.lar.syntactical

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation._
import hmda.validation.dsl.{ Failure, Result, Success }
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource
import hmda.validation.rules.AggregateEditCheck

import scala.concurrent.Future

object S040 extends AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] {

  override def name = "S040"

  override def apply[as: AS, mat: MAT, ec: EC](lars: LoanApplicationRegisterSource): Future[Result] = {
    var larIds: List[String] = List()
    var result: Result = Success()

    val detectDuplicates = lars.map(lar => lar.loan.id)
      .takeWhile(_ => result.isInstanceOf[Success])
      .runForeach { id =>
        if (larIds.contains(id)) {
          result = Failure()
        } else {
          larIds = larIds :+ id
        }
      }

    for {
      _ <- detectDuplicates
    } yield {
      result
    }
  }

}
