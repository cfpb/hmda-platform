package hmda.persistence.serialization.institution

import hmda.persistence.institution.InstitutionPersistence.CreateInstitution
import hmda.persistence.serialization.institution.commands.CreateInstitutionMessage
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
}
