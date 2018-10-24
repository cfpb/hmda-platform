package hmda.persistence.submission

import akka.actor.typed.{ActorContext, Behavior}
import akka.persistence.typed.scaladsl.Effect
import akka.persistence.typed.scaladsl.PersistentBehaviors.CommandHandler
import hmda.messages.submission.SubmissionManagerCommands.SubmissionManagerCommand
import hmda.messages.submission.SubmissionManagerEvents.SubmissionManagerEvent
import hmda.messages.submission.SubmissionProcessingCommands.{
  StartParsing,
  SubmissionProcessingCommand
}
import hmda.messages.submission.SubmissionProcessingEvents.SubmissionProcessingEvent
import hmda.persistence.HmdaTypedPersistentActor

object HmdaParserError
    extends HmdaTypedPersistentActor[SubmissionProcessingCommand,
                                     SubmissionProcessingEvent,
                                     HmdaParserErrorState] {
  override def commandHandler(ctx: ActorContext[SubmissionProcessingCommand])
    : CommandHandler[SubmissionProcessingCommand,
                     SubmissionProcessingEvent,
                     HmdaParserErrorState] = ???

  override def eventHandler: (
      HmdaParserErrorState,
      SubmissionProcessingEvent) => HmdaParserErrorState = ???

  override val name: String = "HmdaParserError"

  override def behavior(
      entityId: String): Behavior[SubmissionProcessingCommand] = ???
}
