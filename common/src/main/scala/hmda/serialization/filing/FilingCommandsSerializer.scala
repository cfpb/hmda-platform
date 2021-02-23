package hmda.serialization.filing

import java.io.NotSerializableException

import akka.actor.ExtendedActorSystem
import akka.actor.typed.ActorRefResolver
import akka.actor.typed.scaladsl.adapter._
import akka.serialization.SerializerWithStringManifest
import hmda.messages.filing.FilingCommands._
import hmda.model.filing.Filing
import hmda.persistence.serialization.filing.FilingMessage
import hmda.serialization.filing.FilingCommandsProtobufConverter._
import hmda.serialization.filing.FilingProtobufConverter._
// $COVERAGE-OFF$
class FilingCommandsSerializer(system: ExtendedActorSystem) extends SerializerWithStringManifest {

  private val resolver = ActorRefResolver(system.toTyped)

  override def identifier: Int = 105

  final val FilingManifest                        = classOf[Filing].getName
  final val CreateFilingManifest                  = classOf[CreateFiling].getName
  final val UpdateFilingStatusManifest            = classOf[UpdateFilingStatus].getName
  final val GetFilingManifest                     = classOf[GetFiling].getName
  final val GetFilingDetailsManifest              = classOf[GetFilingDetails].getName
  final val AddSubmissionManifest                 = classOf[AddSubmission].getName
  final val UpdateSubmissionManifest              = classOf[UpdateSubmission].getName
  final val GetLatestSubmissionManifest           = classOf[GetLatestSubmission].getName
  final val GetLatestSignedSubmissionManifest     = classOf[GetLatestSignedSubmission].getName
  final val GetOldestSignedSubmissionManifest     = classOf[GetOldestSignedSubmission].getName
  final val GetSubmissionSummaryManifest          = classOf[GetSubmissionSummary].getName
  final val GetSubmissionsManifest                = classOf[GetSubmissions].getName
  final val FilingStopManifest                    = classOf[FilingStop].getName

  override def manifest(o: AnyRef): String = o.getClass.getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case f: Filing =>
      filingToProtobuf(f).toByteArray
    case cmd: CreateFiling =>
      createFilingToProtobuf(cmd, resolver).toByteArray
    case cmd: UpdateFilingStatus =>
      updateFilingStatusToProtobuf(cmd, resolver).toByteArray
    case cmd: GetFiling =>
      getFilingToProtobuf(cmd, resolver).toByteArray
    case cmd: GetFilingDetails =>
      getFilingDetailsToProtobuf(cmd, resolver).toByteArray
    case cmd: AddSubmission =>
      addSubmissionToProtobuf(cmd, resolver).toByteArray
    case cmd: UpdateSubmission =>
      updateSubmissionToProtobuf(cmd, resolver).toByteArray
    case cmd: GetLatestSubmission =>
      getLatestSubmissionToProtobuf(cmd, resolver).toByteArray
    case cmd: GetLatestSignedSubmission =>
      getLatestSignedSubmissionToProtobuf(cmd, resolver).toByteArray
    case cmd: GetOldestSignedSubmission =>
      getOldestSignedSubmissionToProtobuf(cmd, resolver).toByteArray
    case cmd: GetSubmissionSummary =>
      getSubmissionSummaryToProtobuf(cmd, resolver).toByteArray
    case cmd: GetSubmissions =>
      getSubmissionsToProtobuf(cmd, resolver).toByteArray
    case FilingStop =>
      filingStopToProtobuf().toByteArray
    case _ =>
      throw new IllegalArgumentException(s"Cannot serialize object of type [${o.getClass.getName}]")

  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef =
    manifest match {
      case FilingManifest =>
        filingFromProtobuf(FilingMessage.parseFrom(bytes))
      case CreateFilingManifest =>
        createFilingFromProtobuf(bytes, resolver)
      case UpdateFilingStatusManifest =>
        updateFilingStatusFromProtobuf(bytes, resolver)
      case GetFilingManifest =>
        getFilingFromProtobuf(bytes, resolver)
      case GetFilingDetailsManifest =>
        getFilingDetailsFromProtobuf(bytes, resolver)
      case AddSubmissionManifest =>
        addSubmissionFromProtobuf(bytes, resolver)
      case UpdateSubmissionManifest =>
        updateSubmissionFromProtobuf(bytes, resolver)
      case GetLatestSubmissionManifest =>
        getLatestSubmissionFromProtobuf(bytes, resolver)
      case GetLatestSignedSubmissionManifest =>
        getLatestSignedSubmissionFromProtobuf(bytes, resolver)
      case GetOldestSignedSubmissionManifest =>
        getOldestSignedSubmissionFromProtobuf(bytes, resolver)
      case GetSubmissionSummaryManifest =>
        getSubmissionSummaryFromProtobuf(bytes, resolver)
      case GetSubmissionsManifest =>
        getSubmissionsFromProtobuf(bytes, resolver)
      case FilingStopManifest =>
        filingStopFromProtobuf()
      case _ =>
        throw new NotSerializableException(s"Unimplemented deserialization of message with manifest [$manifest] in [${getClass.getName}]")
    }

}
// $COVERAGE-ON$