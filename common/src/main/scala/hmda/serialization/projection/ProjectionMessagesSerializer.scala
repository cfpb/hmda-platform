package hmda.serialization.projection

import akka.actor.ExtendedActorSystem
import akka.actor.typed.ActorRefResolver
import akka.actor.typed.scaladsl.adapter._
import akka.serialization.SerializerWithStringManifest
import hmda.messages.projection.CommonProjectionMessages.{ GetOffset, OffsetSaved, SaveOffset }
import hmda.messages.projection.projection.OffsetSavedMessage
import hmda.serialization.projection.ProjectionProtobufConverter._

class ProjectionMessagesSerializer(system: ExtendedActorSystem) extends SerializerWithStringManifest {

  private val resolver = ActorRefResolver(system.toTyped)

  override def identifier: Int = 102

  final val SaveOffsetManifest  = classOf[SaveOffset].getName
  final val GetOffsetManifest   = classOf[GetOffset].getName
  final val OffsetSavedManifest = classOf[OffsetSaved].getName

  override def manifest(o: AnyRef): String = o.getClass.getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case cmd: SaveOffset =>
      saveOffsetToProtobuf(cmd, resolver).toByteArray
    case cmd: GetOffset =>
      getOffsetToProtobuf(cmd, resolver).toByteArray
    case evt: OffsetSaved =>
      offsetSavedToProtobuf(evt).toByteArray
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef =
    manifest match {
      case SaveOffsetManifest =>
        saveOffsetFromProtobuf(bytes, resolver)
      case GetOffsetManifest =>
        getOffsetFromProtobuf(bytes, resolver)
      case OffsetSavedManifest =>
        offsetSavedFromProtobuf(OffsetSavedMessage.parseFrom(bytes))
    }
}
