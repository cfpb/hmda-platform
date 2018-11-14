package hmda.persistence

import akka.actor.typed.ActorContext
import akka.persistence.typed.scaladsl.PersistentBehavior.CommandHandler
import hmda.actor.HmdaTypedActor

trait HmdaTypedPersistentActor[C, E, S] extends HmdaTypedActor[C] {

  def commandHandler(ctx: ActorContext[C]): CommandHandler[C, E, S]

  def eventHandler: (S, E) => S

}
