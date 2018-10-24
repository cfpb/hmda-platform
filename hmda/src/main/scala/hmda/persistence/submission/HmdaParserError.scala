package hmda.persistence.submission

import akka.actor.typed.{ActorContext, Behavior}
import akka.persistence.typed.scaladsl.PersistentBehaviors.CommandHandler
import hmda.messages.submission.HmdaParserErrorCommands.HmdaParserErrorCommand
import hmda.messages.submission.HmdaParserErrorEvents.HmdaParserErrorEvent
import hmda.persistence.HmdaTypedPersistentActor

object HmdaParserError
    extends HmdaTypedPersistentActor[HmdaParserErrorCommand,
                                     HmdaParserErrorEvent,
                                     HmdaParserErrorState] {
  override def commandHandler(ctx: ActorContext[HmdaParserErrorCommand])
    : CommandHandler[HmdaParserErrorCommand,
                     HmdaParserErrorEvent,
                     HmdaParserErrorState] = ???

  override def eventHandler
    : (HmdaParserErrorState, HmdaParserErrorEvent) => HmdaParserErrorState = ???

  override val name: String = ???

  override def behavior(entityId: String): Behavior[HmdaParserErrorCommand] =
    ???
}
