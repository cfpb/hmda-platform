package hmda.serialization.submission

import java.io.NotSerializableException

import akka.actor.ExtendedActorSystem
import akka.actor.typed.ActorRefResolver
import akka.actor.typed.scaladsl.adapter._
import akka.serialization.SerializerWithStringManifest
import hmda.messages.submission.EditDetailsCommands.{ GetEditDetails, GetEditRowCount, PersistEditDetails }
import hmda.persistence.serialization.edit.details.commands.{ GetEditDetailsMessage, GetEditRowCountMessage, PersistEditDetailsMessage }
import hmda.serialization.submission.EditDetailsCommandsProtobufConverter._

class EditDetailsCommandsSerializer(system: ExtendedActorSystem) extends SerializerWithStringManifest {

  private val resolver = ActorRefResolver(system.toTyped)

  override def identifier: Int = 112

  final val PersistEditDetailsManifest = classOf[PersistEditDetails].getName
  final val GetEditRowCountManifest    = classOf[GetEditRowCount].getName
  final val GetEditDetailsManifest     = classOf[GetEditDetails].getName

  override def manifest(o: AnyRef): String = o.getClass.getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case cmd: PersistEditDetails =>
      persistEditDetailsToProtobuf(cmd, resolver).toByteArray
    case cmd: GetEditRowCount =>
      getEditRowCountToProtobuf(cmd, resolver).toByteArray
    case cmd: GetEditDetails =>
      getEditDetailsToProtobuf(cmd, resolver).toByteArray
    case _ =>
      throw new IllegalArgumentException(s"Cannot serialize object of type [${o.getClass.getName}]")

  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef =
    manifest match {
      case PersistEditDetailsManifest =>
        persistEditDetailsFromProtobuf(PersistEditDetailsMessage.parseFrom(bytes), resolver)
      case GetEditRowCountManifest =>
        getEditRowCountFromProtobuf(GetEditRowCountMessage.parseFrom(bytes), resolver)
      case GetEditDetailsManifest =>
        getEditDetailsFromProtobuf(GetEditDetailsMessage.parseFrom(bytes), resolver)
      case _ =>
        throw new NotSerializableException(s"Unimplemented deserialization of message with manifest [$manifest] in [${getClass.getName}]")
    }
}
