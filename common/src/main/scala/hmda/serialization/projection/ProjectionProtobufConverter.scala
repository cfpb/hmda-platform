package hmda.serialization.projection

import akka.actor.typed.ActorRefResolver
import hmda.messages.projection.CommonProjectionMessages.{
  GetOffset,
  OffsetSaved,
  SaveOffset
}
import hmda.messages.projection.projection.{
  GetOffsetMessage,
  OffsetSavedMessage,
  SaveOffsetMessage
}

object ProjectionProtobufConverter {

  def saveOffsetToProtobuf(cmd: SaveOffset,
                           resolver: ActorRefResolver): SaveOffsetMessage = {
    SaveOffsetMessage(
      seqNr = cmd.seqNr,
      replyTo = resolver.toSerializationFormat(cmd.replyTo)
    )
  }

  def saveOffsetFromProtobuf(bytes: Array[Byte],
                             resolver: ActorRefResolver): SaveOffset = {
    val msg = SaveOffsetMessage.parseFrom(bytes)
    val actorRef = resolver.resolveActorRef(msg.replyTo)
    SaveOffset(msg.seqNr, actorRef)
  }

  def getOffsetToProtobuf(cmd: GetOffset,
                          resolver: ActorRefResolver): GetOffsetMessage = {
    GetOffsetMessage(
      replyTo = resolver.toSerializationFormat(cmd.replyTo)
    )
  }

  def getOffsetFromProtobuf(bytes: Array[Byte],
                            resolver: ActorRefResolver): GetOffset = {
    val msg = GetOffsetMessage.parseFrom(bytes)
    val actorRef = resolver.resolveActorRef(msg.replyTo)
    GetOffset(actorRef)
  }

  def offsetSavedToProtobuf(evt: OffsetSaved): OffsetSavedMessage = {
    OffsetSavedMessage(
      seqNr = evt.seqNr
    )
  }

  def offsetSavedFromProtobuf(msg: OffsetSavedMessage): OffsetSaved = {
    OffsetSaved(msg.seqNr)
  }
}
