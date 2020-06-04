package hmda.serialization.submission

import java.io.NotSerializableException

import akka.actor.ExtendedActorSystem
import akka.actor.typed.ActorRefResolver
import akka.actor.typed.scaladsl.adapter._
import akka.serialization.SerializerWithStringManifest
import hmda.messages.submission.SubmissionCommands.{ CreateSubmission, GetSubmission, ModifySubmission, SubmissionStop }
import hmda.model.filing.submission.Submission
import hmda.persistence.serialization.submission.SubmissionMessage
import hmda.persistence.serialization.submission.commands.SubmissionStopMessage
import hmda.serialization.submission.SubmissionCommandsProtobufConverter._
import hmda.serialization.submission.SubmissionProtobufConverter._
// $COVERAGE-OFF$
class SubmissionCommandsSerializer(system: ExtendedActorSystem) extends SerializerWithStringManifest {

  private val resolver = ActorRefResolver(system.toTyped)

  override def identifier: Int = 103

  final val SubmissionManifest       = classOf[Submission].getName
  final val CreateSubmissionManifest = classOf[CreateSubmission].getName
  final val ModifySubmissionManifest = classOf[ModifySubmission].getName
  final val GetSubmissionManifest    = classOf[GetSubmission].getName
  final val SubmissionStopManifest   = classOf[SubmissionStop].getName

  override def manifest(o: AnyRef): String = o.getClass.getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case s: Submission =>
      submissionToProtobuf(s).toByteArray
    case cmd: CreateSubmission =>
      createSubmissionToProtobuf(cmd, resolver).toByteArray
    case cmd: ModifySubmission =>
      modifySubmissionToProtobuf(cmd, resolver).toByteArray
    case cmd: GetSubmission =>
      getSubmissionToProtobuf(cmd, resolver).toByteArray
    case cmd: SubmissionStop =>
      submissionStopToProtobuf(cmd).toByteArray
    case _ =>
      throw new IllegalArgumentException(s"Cannot serialize object of type [${o.getClass.getName}]")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef =
    manifest match {
      case SubmissionManifest =>
        submissionFromProtobuf(SubmissionMessage.parseFrom(bytes))
      case CreateSubmissionManifest =>
        createSubmissionFromProtobuf(bytes, resolver)
      case ModifySubmissionManifest =>
        modifySubmisstionFromProtobuf(bytes, resolver)
      case GetSubmissionManifest =>
        getSubmissionFromProtobuf(bytes, resolver)
      case SubmissionStopManifest =>
        submissionStopFromProtobuf(SubmissionStopMessage.parseFrom(bytes))
      case _ =>
        throw new NotSerializableException(s"Unimplemented deserialization of message with manifest [$manifest] in [${getClass.getName}]")
    }

}
// $COVERAGE-ON$
