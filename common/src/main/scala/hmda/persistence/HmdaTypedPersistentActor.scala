package hmda.persistence

import akka.actor.typed.TypedActorContext
import akka.persistence.typed.scaladsl.EventSourcedBehavior.CommandHandler
import hmda.actor.HmdaTypedActor

trait HmdaTypedPersistentActor[C, E, S] extends HmdaTypedActor[C] {

  def commandHandler(ctx: TypedActorContext[C]): CommandHandler[C, E, S]

  def eventHandler: (S, E) => S

}
