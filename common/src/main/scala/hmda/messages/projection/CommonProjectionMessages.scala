package hmda.messages.projection

import akka.actor.typed.ActorRef
import akka.persistence.query.Offset

object CommonProjectionMessages {
  sealed trait ProjectionCommand
  sealed trait ProjectionEvent

  final case object StartStreaming                                            extends ProjectionCommand
  final case class SaveOffset(offset: Offset, replyTo: ActorRef[OffsetSaved]) extends ProjectionCommand
  final case class GetOffset(replyTo: ActorRef[OffsetSaved])                  extends ProjectionCommand
  final case class OffsetSaved(offset: Offset)                                extends ProjectionEvent
}
