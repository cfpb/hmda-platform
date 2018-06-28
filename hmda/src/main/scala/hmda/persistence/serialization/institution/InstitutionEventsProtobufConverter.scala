package hmda.persistence.serialization.institution

import InstitutionProtobufConverter._
import hmda.persistence.institution.InstitutionPersistence.{
  InstitutionCreated,
  InstitutionDeleted,
  InstitutionModified,
  InstitutionNotExists
}
import hmda.persistence.serialization.institution.events.{
  InstitutionCreatedMessage,
  InstitutionDeletedMessage,
  InstitutionModifiedMessage,
  InstitutionNotExistsMessage
}

object InstitutionEventsProtobufConverter {

  def institutionCreatedToProtobuf(
      evt: InstitutionCreated): InstitutionCreatedMessage = {
    InstitutionCreatedMessage(
      institution = Some(institutionToProtobuf(evt.i))
    )
  }

  def institutionCreatedFromProtobuf(
      msg: InstitutionCreatedMessage): InstitutionCreated = {
    InstitutionCreated(
      i =
        institutionFromProtobuf(msg.institution.getOrElse(InstitutionMessage()))
    )
  }

  def institutionModifiedToProtobuf(
      evt: InstitutionModified): InstitutionModifiedMessage = {
    InstitutionModifiedMessage(
      institution = Some(institutionToProtobuf(evt.i))
    )
  }

  def institutionModifiedFromProtobuf(
      msg: InstitutionModifiedMessage): InstitutionModified = {
    InstitutionModified(
      i =
        institutionFromProtobuf(msg.institution.getOrElse(InstitutionMessage()))
    )
  }

  def institutionDeletedToProtobuf(
      evt: InstitutionDeleted): InstitutionDeletedMessage = {
    InstitutionDeletedMessage(
      lEI = evt.LEI
    )
  }

  def institutionDeletedFromProtobuf(
      msg: InstitutionDeletedMessage): InstitutionDeleted = {
    InstitutionDeleted(
      LEI = msg.lEI
    )
  }

  def institutionNotExistsToProtobuf(
      evt: InstitutionNotExists.type): InstitutionNotExistsMessage = {
    InstitutionNotExistsMessage.defaultInstance
  }

  def institutionNotExistsFromProtobuf(
      msg: InstitutionNotExistsMessage): InstitutionNotExists.type = {
    InstitutionNotExists
  }

}
