package hmda.persistence.serialization.institution

import hmda.persistence.institution.InstitutionPersistence._
import hmda.persistence.serialization.institution.commands.{
  CreateInstitutionMessage,
  DeleteInstitutionMessage,
  GetMessage,
  ModifyInstitutionMessage
}
import InstitutionProtobufConverter._
import akka.actor.typed.ActorRefResolver

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
      cmd: Get,
      resolver: ActorRefResolver
  ): GetMessage = {
    GetMessage(
      resolver.toSerializationFormat(cmd.replyTo)
    )
  }

  def getInstitutionFromProtobuf(
      bytes: Array[Byte],
      resolver: ActorRefResolver
  ): Get = {
    val msg = GetMessage.parseFrom(bytes)
    val actorRef = resolver.resolveActorRef(msg.replyTo)
    Get(actorRef)
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
