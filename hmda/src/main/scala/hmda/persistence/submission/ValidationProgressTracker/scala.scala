package hmda.persistence.submission

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{ Behavior, LogOptions, Terminated }
import akka.actor.{ ActorRef => ClassicActorRef }
import hmda.messages.submission.ValidationProgressTrackerCommands._
import hmda.model.processing.state.{ HmdaValidationErrorState, ValidationProgressTrackerState, ValidationType, VerificationType }
import org.slf4j.event.Level

object ValidationProgressTracker {
  private def publishUpdate(subscribers: Set[ClassicActorRef], state: ValidationProgressTrackerState): Unit =
    subscribers.foreach(subscriber => subscriber ! state)

  private def behavior(
                        state: ValidationProgressTrackerState,
                        subscribers: Set[ClassicActorRef]
                      ): Behavior[ValidationProgressTrackerCommand] =
    Behaviors.logMessages(
      LogOptions().withEnabled(true).withLevel(Level.ERROR),
      Behaviors.setup { ctx =>
        ctx.log.debug(s"Starting tracker: ${ctx.self.path.name}")

        Behaviors
          .receiveMessage[ValidationProgressTrackerCommand] {
            case StateSnapshot(incoming) =>
              val newState = state.fromSnapshot(incoming)
              publishUpdate(subscribers, newState)
              behavior(newState, subscribers)

            case NumberOfLines(lines) =>
              val newState = state.updateLines(lines)
              publishUpdate(subscribers, newState)
              behavior(newState, subscribers)

            case ValidationDelta(ValidationType.Quality, validationProgress) =>
              val newState = state.updateQuality(validationProgress)
              publishUpdate(subscribers, newState)
              behavior(newState, subscribers)

            case ValidationDelta(ValidationType.Syntactical, validationProgress) =>
              val newState = state.updateSyntactical(validationProgress)
              publishUpdate(subscribers, newState)
              behavior(newState, subscribers)

            case ValidationDelta(ValidationType.Macro, validationProgress) =>
              val newState = state.updateMacro(validationProgress)
              publishUpdate(subscribers, newState)
              behavior(newState, subscribers)

            case VerifyDelta(VerificationType.Quality, verification) =>
              val newState = state.copy(qualityVerified = verification)
              publishUpdate(subscribers, newState)
              behavior(newState, subscribers)

            case VerifyDelta(VerificationType.Macro, verification) =>
              val newState = state.copy(macroVerified = verification)
              publishUpdate(subscribers, newState)
              behavior(newState, subscribers)

            case SignedDelta(submissionSigned) =>
              val newState = state.copy(submissionSigned = submissionSigned)
              publishUpdate(subscribers, newState)
              behavior(newState, subscribers)

            case Poll(replyTo) =>
              replyTo ! state
              behavior(state, subscribers)

            case Subscribe(replyTo) =>
              ctx.watch(replyTo)
              val classic = replyTo.toClassic
              publishUpdate(Set(classic), state) // only message the new subscriber
              behavior(state, subscribers + classic)
          }
          .receiveSignal {
            // Keep track of subscribers who aren't interested anymore
            case (_, Terminated(subscriber)) =>
              behavior(state, subscribers - subscriber.toClassic)
          }
      }
    )

  def apply(state: HmdaValidationErrorState): Behavior[ValidationProgressTrackerCommand] =
    behavior(ValidationProgressTrackerState.initialize(state), Set.empty)
}