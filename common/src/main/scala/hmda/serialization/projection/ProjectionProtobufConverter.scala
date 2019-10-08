package hmda.serialization.projection

import java.util.UUID

import akka.actor.typed.ActorRefResolver
import akka.persistence.query.TimeBasedUUID
import hmda.messages.projection.CommonProjectionMessages.{ GetOffset, OffsetSaved, SaveOffset }
import hmda.messages.projection.projection.{ GetOffsetMessage, OffsetSavedMessage, SaveOffsetMessage }

object ProjectionProtobufConverter {

  def saveOffsetToProtobuf(cmd: SaveOffset, resolver: ActorRefResolver): SaveOffsetMessage = {
    val offset = cmd.offset match {
      case TimeBasedUUID(uuid) => uuid.toString
    }
    SaveOffsetMessage(
      offset = offset,
      replyTo = resolver.toSerializationFormat(cmd.replyTo)
    )
  }

  def saveOffsetFromProtobuf(bytes: Array[Byte], resolver: ActorRefResolver): SaveOffset = {
    val msg      = SaveOffsetMessage.parseFrom(bytes)
    val actorRef = resolver.resolveActorRef(msg.replyTo)
    SaveOffset(TimeBasedUUID(UUID.fromString(msg.offset)), actorRef)
  }

  def getOffsetToProtobuf(cmd: GetOffset, resolver: ActorRefResolver): GetOffsetMessage =
    GetOffsetMessage(
      replyTo = resolver.toSerializationFormat(cmd.replyTo)
    )

  def getOffsetFromProtobuf(bytes: Array[Byte], resolver: ActorRefResolver): GetOffset = {
    val msg      = GetOffsetMessage.parseFrom(bytes)
    val actorRef = resolver.resolveActorRef(msg.replyTo)
    GetOffset(actorRef)
  }

  def offsetSavedToProtobuf(evt: OffsetSaved): OffsetSavedMessage = {
    val offset = evt.offset match {
      case TimeBasedUUID(uuid) => uuid.toString
    }
    OffsetSavedMessage(
      offset = offset
    )
  }

  def offsetSavedFromProtobuf(msg: OffsetSavedMessage): OffsetSaved =
    OffsetSaved(TimeBasedUUID(UUID.fromString(msg.offset)))
}
