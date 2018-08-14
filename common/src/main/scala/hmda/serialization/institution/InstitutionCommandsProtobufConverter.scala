package hmda.serialization.institution

import akka.actor.typed.ActorRefResolver
import hmda.messages.institution.InstitutionCommands.{
  CreateInstitution,
  DeleteInstitution,
  GetInstitution,
  ModifyInstitution
}
import hmda.messages.institution.InstitutionEvents.InstitutionEvent
import hmda.persistence.serialization.institution.InstitutionMessage
import hmda.persistence.serialization.institution.commands.{
  CreateInstitutionMessage,
  DeleteInstitutionMessage,
  GetInstitutionMessage,
  ModifyInstitutionMessage
}
import InstitutionProtobufConverter._

object InstitutionCommandsProtobufConverter {

  def createInstitutionToProtobuf(
      cmd: CreateInstitution,
      resolver: ActorRefResolver): CreateInstitutionMessage = {
    CreateInstitutionMessage(
      institution = Some(institutionToProtobuf(cmd.i)),
      replyTo = resolver.toSerializationFormat(cmd.replyTo)
    )
  }

  def createInstitutionFromProtobuf(
      bytes: Array[Byte],
      resolver: ActorRefResolver): CreateInstitution = {
    val msg = CreateInstitutionMessage.parseFrom(bytes)
    val institution = institutionFromProtobuf(
      msg.institution.getOrElse(InstitutionMessage()))
    val actorRef = resolver.resolveActorRef(msg.replyTo)
    CreateInstitution(institution, actorRef)
  }

  def modifyInstitutionToProtobuf(
      cmd: ModifyInstitution,
      resolver: ActorRefResolver
  ): ModifyInstitutionMessage = {
    ModifyInstitutionMessage(
      institution = Some(institutionToProtobuf(cmd.i)),
      replyTo = resolver.toSerializationFormat(cmd.replyTo)
    )
  }

  def modifyInstitutionFromProtobuf(
      bytes: Array[Byte],
      resolver: ActorRefResolver
  ): ModifyInstitution = {
    val msg = ModifyInstitutionMessage.parseFrom(bytes)
    val institution = institutionFromProtobuf(
      msg.institution.getOrElse(InstitutionMessage()))
    val actorRef = resolver.resolveActorRef[InstitutionEvent](msg.replyTo)
    ModifyInstitution(institution, actorRef)
  }

  def getInstitutionToProtobuf(
      cmd: GetInstitution,
      resolver: ActorRefResolver
  ): GetInstitutionMessage = {
    GetInstitutionMessage(
      resolver.toSerializationFormat(cmd.replyTo)
    )
  }

  def getInstitutionFromProtobuf(
      bytes: Array[Byte],
      resolver: ActorRefResolver
  ): GetInstitution = {
    val msg = GetInstitutionMessage.parseFrom(bytes)
    val actorRef = resolver.resolveActorRef(msg.replyTo)
    GetInstitution(actorRef)
  }

  def deleteInstitutionToProtobuf(
      cmd: DeleteInstitution,
      resolver: ActorRefResolver
  ): DeleteInstitutionMessage = {
    DeleteInstitutionMessage(
      cmd.LEI,
      resolver.toSerializationFormat(cmd.replyTo)
    )
  }

  def deleteInstitutionFromProtobuf(
      bytes: Array[Byte],
      resolver: ActorRefResolver
  ): DeleteInstitution = {
    val msg = DeleteInstitutionMessage.parseFrom(bytes)
    val actorRef = resolver.resolveActorRef(msg.replyTo)
    DeleteInstitution(msg.lei, actorRef)
  }
}
