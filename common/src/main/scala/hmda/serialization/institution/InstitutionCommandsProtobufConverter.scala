package hmda.serialization.institution

import akka.actor.typed.ActorRefResolver
import hmda.messages.institution.InstitutionCommands._
import hmda.messages.institution.InstitutionEvents.InstitutionEvent
import hmda.model.institution.InstitutionDetail
import hmda.persistence.serialization.filing.FilingMessage
import hmda.persistence.serialization.institution.InstitutionMessage
import hmda.persistence.serialization.institution.commands._
import hmda.serialization.filing.FilingProtobufConverter._
import hmda.serialization.institution.InstitutionProtobufConverter._
// $COVERAGE-OFF$
object InstitutionCommandsProtobufConverter {

  def createInstitutionToProtobuf(cmd: CreateInstitution, resolver: ActorRefResolver): CreateInstitutionMessage =
    CreateInstitutionMessage(
      institution = Some(institutionToProtobuf(cmd.i)),
      replyTo = resolver.toSerializationFormat(cmd.replyTo)
    )

  def createInstitutionFromProtobuf(bytes: Array[Byte], resolver: ActorRefResolver): CreateInstitution = {
    val msg         = CreateInstitutionMessage.parseFrom(bytes)
    val institution = institutionFromProtobuf(msg.institution.getOrElse(InstitutionMessage()))
    val actorRef    = resolver.resolveActorRef(msg.replyTo)
    CreateInstitution(institution, actorRef)
  }

  def modifyInstitutionToProtobuf(
    cmd: ModifyInstitution,
    resolver: ActorRefResolver
  ): ModifyInstitutionMessage =
    ModifyInstitutionMessage(
      institution = Some(institutionToProtobuf(cmd.i)),
      replyTo = resolver.toSerializationFormat(cmd.replyTo)
    )

  def modifyInstitutionFromProtobuf(
    bytes: Array[Byte],
    resolver: ActorRefResolver
  ): ModifyInstitution = {
    val msg         = ModifyInstitutionMessage.parseFrom(bytes)
    val institution = institutionFromProtobuf(msg.institution.getOrElse(InstitutionMessage()))
    val actorRef    = resolver.resolveActorRef[InstitutionEvent](msg.replyTo)
    ModifyInstitution(institution, actorRef)
  }

  def getInstitutionToProtobuf(
    cmd: GetInstitution,
    resolver: ActorRefResolver
  ): GetInstitutionMessage =
    GetInstitutionMessage(
      resolver.toSerializationFormat(cmd.replyTo)
    )

  def getInstitutionFromProtobuf(
    bytes: Array[Byte],
    resolver: ActorRefResolver
  ): GetInstitution = {
    val msg      = GetInstitutionMessage.parseFrom(bytes)
    val actorRef = resolver.resolveActorRef(msg.replyTo)
    GetInstitution(actorRef)
  }

  def getInstitutionDetailsToProtobuf(cmd: GetInstitutionDetails, resolver: ActorRefResolver): GetInstitutionDetailsMessage =
    GetInstitutionDetailsMessage(resolver.toSerializationFormat(cmd.replyTo))

  def getInstitutionDetailsFromProtobuf(bytes: Array[Byte], resolver: ActorRefResolver): GetInstitutionDetails = {
    val msg = GetInstitutionDetailsMessage.parseFrom(bytes)
    val actorRef =
      resolver.resolveActorRef[Option[InstitutionDetail]](msg.replyTo)
    GetInstitutionDetails(actorRef)
  }

  def deleteInstitutionToProtobuf(
    cmd: DeleteInstitution,
    resolver: ActorRefResolver
  ): DeleteInstitutionMessage =
    DeleteInstitutionMessage(
      cmd.LEI,
      cmd.activityYear,
      resolver.toSerializationFormat(cmd.replyTo)
    )

  def deleteInstitutionFromProtobuf(
    bytes: Array[Byte],
    resolver: ActorRefResolver
  ): DeleteInstitution = {
    val msg      = DeleteInstitutionMessage.parseFrom(bytes)
    val actorRef = resolver.resolveActorRef(msg.replyTo)
    DeleteInstitution(msg.lei, msg.activityYear, actorRef)
  }

  def addFilingFromProtobuf(bytes: Array[Byte], refResolver: ActorRefResolver): AddFiling = {
    val msg = AddFilingMessage.parseFrom(bytes)
    AddFiling(
      filingFromProtobuf(msg.filing.getOrElse(FilingMessage())),
      if (msg.replyTo == "") None
      else Some(refResolver.resolveActorRef(msg.replyTo))
    )
  }

  def addFilingToProtobuf(cmd: AddFiling, refResolver: ActorRefResolver): AddFilingMessage = {
    val filing = cmd.filing
    AddFilingMessage(
      if (filing.isEmpty) None
      else Some(filingToProtobuf(cmd.filing)),
      cmd.replyTo match {
        case None      => ""
        case Some(ref) => refResolver.toSerializationFormat(ref)
      }
    )
  }

}
// $COVERAGE-OFF$