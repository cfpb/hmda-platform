package hmda.persistence.submission

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{ Behavior, Terminated }
import akka.actor.{ ActorRef => ClassicActorRef }
import hmda.messages.submission.ValidationProgressTrackerCommands._
import hmda.model.processing.state.{ HmdaValidationErrorState, ValidationProgressTrackerState, ValidationType }
// $COVERAGE-OFF$
/**
 * This actor is responsible for keeping track of the file's validation progress and is responsible for publishing
 * updates to interested parties (i.e. WebSocketProgressTracker actors that are created when a websocket connection
 * is established in order to listen for progress updates for the file validation progress)
 */
object ValidationProgressTracker {
  private def publishUpdate(subscribers: Set[ClassicActorRef], state: ValidationProgressTrackerState): Unit =
    subscribers.foreach(subscriber => subscriber ! state)

  private def behavior(
                        state: ValidationProgressTrackerState,
                        subscribers: Set[ClassicActorRef]
                      ): Behavior[ValidationProgressTrackerCommand] =
    Behaviors.setup { ctx =>
      ctx.log.debug(s"Starting tracker: ${ctx.self.path.name}")

      Behaviors
        .receiveMessage[ValidationProgressTrackerCommand] {
          case StateSnapshot(incoming) =>
            val newState = state.fromSnapshot(incoming)
            publishUpdate(subscribers, newState)
            behavior(newState, subscribers)

          case ValidationDelta(ValidationType.Quality, validationProgress, editNames) =>
            val newState = state.updateQuality(validationProgress, editNames)
            publishUpdate(subscribers, newState)
            behavior(newState, subscribers)

          case ValidationDelta(ValidationType.Syntactical, validationProgress, editNames) =>
            val newState = state.updateSyntactical(validationProgress, editNames)
            publishUpdate(subscribers, newState)
            behavior(newState, subscribers)

          case ValidationDelta(ValidationType.Macro, validationProgress, editNames) =>
            val newState = state.updateMacro(validationProgress, editNames)
            publishUpdate(subscribers, newState)
            behavior(newState, subscribers)

          case Poll(replyTo) =>
            replyTo ! state
            Behaviors.same

          case Subscribe(replyTo) =>
            ctx.watch(replyTo)
            ctx.log.debug(s"Subscriber ${replyTo.path.name} has subscribed for updates")
            val classic = replyTo.toClassic
            publishUpdate(Set(classic), state) // only message the new subscriber
            behavior(state, subscribers + classic)
        }
        .receiveSignal {
          // Keep track of subscribers who aren't interested anymore
          case (_, Terminated(subscriber)) =>
            ctx.log.debug(s"Subscriber ${subscriber.path.name} has disconnected")
            behavior(state, subscribers - subscriber.toClassic)
        }
    }

  def apply(state: HmdaValidationErrorState): Behavior[ValidationProgressTrackerCommand] =
    behavior(ValidationProgressTrackerState.initialize(state), Set.empty)
}
// $COVERAGE-ON$