package hmda.persistence

import org.apache.pekko.actor.typed.scaladsl.ActorContext
import pekko.persistence.typed.scaladsl.EventSourcedBehavior.CommandHandler
import hmda.actor.HmdaTypedActor

trait HmdaTypedPersistentActor[C, E, S] extends HmdaTypedActor[C] {

  def commandHandler(ctx: ActorContext[C]): CommandHandler[C, E, S]

  def eventHandler: (S, E) => S

}