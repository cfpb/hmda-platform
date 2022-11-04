package hmda.serialization.validation

import akka.actor.ExtendedActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.ActorRefResolver
import akka.serialization.SerializerWithStringManifest
import hmda.messages.submission.ValidationProgressTrackerCommands
import hmda.model.processing.state.ValidationProgressTrackerState
import hmda.persistence.serialization.validationProgressTracker.{ValidationProgressTrackerPollMessage, ValidationProgressTrackerStateMessage, ValidationProgressTrackerSubscribeMessage}
import hmda.serialization.validation.ValidationProgressTrackerCommandsProtobufConverter.{pollFromProtobuf, pollToProtobuf, stateFromProtobuf, stateToProtobuf, subscribeFromProtobuf, subscribeToProtobuf}

import java.io.NotSerializableException

class ValidationProgressTrackerCommandsSerializer(system: ExtendedActorSystem) extends SerializerWithStringManifest {
  private val resolver = ActorRefResolver(system.toTyped)

  override def identifier: Int = 119

  override def manifest(o: AnyRef): String = o.getClass.getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case cmd: ValidationProgressTrackerCommands.Subscribe => subscribeToProtobuf(cmd, resolver).toByteArray
    case cmd: ValidationProgressTrackerCommands.Poll => pollToProtobuf(cmd, resolver).toByteArray
    case cmd: ValidationProgressTrackerState => stateToProtobuf(cmd).toByteArray
    case _ => throw new IllegalArgumentException(s"Cannot serialize object of type [${o.getClass.getName}]")
  }

  final val SubscribeManifest  = classOf[ValidationProgressTrackerCommands.Subscribe].getName
  final val PollManifest  = classOf[ValidationProgressTrackerCommands.Poll].getName
  final val StateManifest  = classOf[ValidationProgressTrackerState].getName

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case SubscribeManifest => subscribeFromProtobuf(ValidationProgressTrackerSubscribeMessage.parseFrom(bytes), resolver)
    case PollManifest => pollFromProtobuf(ValidationProgressTrackerPollMessage.parseFrom(bytes), resolver)
    case StateManifest => stateFromProtobuf(ValidationProgressTrackerStateMessage.parseFrom(bytes))
    case _ => throw new NotSerializableException(s"Unimplemented deserialization of message with manifest [$manifest] in [${getClass.getName}]")
  }
}