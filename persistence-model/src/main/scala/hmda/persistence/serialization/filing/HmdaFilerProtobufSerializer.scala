package hmda.persistence.serialization.filing

import akka.serialization.SerializerWithStringManifest
import hmda.persistence.messages.commands.institutions.HmdaFilerCommands.{ CreateHmdaFiler, DeleteHmdaFiler, FindHmdaFiler, FindHmdaFilers }
import hmda.persistence.messages.events.institutions.HmdaFilerEvents.{ HmdaFilerCreated, HmdaFilerDeleted }
import HmdaFilerProtobufConverter._
import hmda.persistence.model.serialization.HmdaFilerCommands.{ CreateHmdaFilerMessage, DeleteHmdaFilerMessage, FindHmdaFilerMessage, FindHmdaFilersMessage }
import hmda.persistence.model.serialization.HmdaFilerEvents.{ HmdaFilerCreatedMessage, HmdaFilerDeletedMessage }

class HmdaFilerProtobufSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 1020

  override def manifest(o: AnyRef): String = o.getClass.getName

  final val CreateHmdaFilerManifest = classOf[CreateHmdaFiler].getName
  final val DeleteHmdaFilerManifest = classOf[DeleteHmdaFiler].getName
  final val FindHmdaFilerManifest = classOf[FindHmdaFiler].getName
  final val HmdaFilerCreatedManifest = classOf[HmdaFilerCreated].getName
  final val HmdaFilerDeletedManifest = classOf[HmdaFilerDeleted].getName
  final val FindHmdaFilersManifest = classOf[FindHmdaFilers].getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case cmd: CreateHmdaFiler => createHmdaFilerToProtobuf(cmd).toByteArray
    case cmd: DeleteHmdaFiler => deleteHmdaFilerToProtobuf(cmd).toByteArray
    case cmd: FindHmdaFiler => findHmdaFilerToProtobuf(cmd).toByteArray
    case cmd: FindHmdaFilers => findHmdaFilersToProtobuf(cmd).toByteArray
    case evt: HmdaFilerCreated => hmdaFilerCreatedToProtobuf(evt).toByteArray
    case evt: HmdaFilerDeleted => hmdaFilerDeletedToProtobuf(evt).toByteArray
    case msg: Any => throw new RuntimeException(s"Cannot serialize this message: ${msg.toString}")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case CreateHmdaFilerManifest => createHmdaFilerFromProtobuf(CreateHmdaFilerMessage.parseFrom(bytes))
    case DeleteHmdaFilerManifest => deleteHmdaFilerFromProtobuf(DeleteHmdaFilerMessage.parseFrom(bytes))
    case FindHmdaFilerManifest => findHmdaFilerFromProtobuf(FindHmdaFilerMessage.parseFrom(bytes))
    case HmdaFilerCreatedManifest => hmdaFilerCreatedFromProtobuf(HmdaFilerCreatedMessage.parseFrom(bytes))
    case HmdaFilerDeletedManifest => hmdaFilerDeletedFromProtobuf(HmdaFilerDeletedMessage.parseFrom(bytes))
    case FindHmdaFilersManifest => findHmdaFilersFromProtobuf(FindHmdaFilersMessage.parseFrom(bytes))
    case msg: Any => throw new RuntimeException(s"Cannot deserialize this message: ${msg.toString}")
  }
}
