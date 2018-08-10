package hmda.messages.projection

import akka.actor.typed.ActorRef

object CommonProjectionMessages {
  sealed trait ProjectionCommand
  sealed trait ProjectionEvent

  case object StartStreaming extends ProjectionCommand
  final case class SaveOffset(seqNr: Long, replyTo: ActorRef[OffsetSaved])
      extends ProjectionCommand
  final case class GetOffset(replyTo: ActorRef[OffsetSaved])
      extends ProjectionCommand
  final case class OffsetSaved(seqNr: Long) extends ProjectionEvent
}
