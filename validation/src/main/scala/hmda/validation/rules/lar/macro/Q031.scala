package hmda.validation.rules.lar.`macro`

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.rules.lar.`macro`.MacroEditTypes._

import scala.concurrent.{ ExecutionContext, Future }

object Q031 extends AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] {

  val config = ConfigFactory.load()
  val larCount = config.getInt("hmda.validation.macro.Q031.numOfLars")
  val multifamilyCount = config.getInt("hmda.validation.macro.Q031.numOfMultifamily")

  override def name = "Q031"

  override def apply(lars: LoanApplicationRegisterSource)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Future[Result] = {

    val multifamily =
      count(lars.filter(lar => lar.loan.propertyType == 3))

    val total = count(lars)

    for {
      m <- multifamily
      t <- total
    } yield {
      when(t is lessThan(larCount)) {
        m is lessThan(multifamilyCount)
      }
    }

  }
}
