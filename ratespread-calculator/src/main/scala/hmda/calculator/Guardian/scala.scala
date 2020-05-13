package hmda.calculator

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import hmda.calculator.api.http.RateSpreadAPI
import hmda.calculator.scheduler.APORScheduler
import hmda.calculator.scheduler.APORScheduler.Command

// $COVERAGE-OFF$
object Guardian {
  val name = "ratespread-calculator-guardian"

  def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { ctx =>
    val scheduler = ctx.spawn(APORScheduler(), APORScheduler.name)
    scheduler ! Command.Initialize

    ctx.spawn[Nothing](RateSpreadAPI(), RateSpreadAPI.name)

    Behaviors.ignore
  }
}
// $COVERAGE-ON$