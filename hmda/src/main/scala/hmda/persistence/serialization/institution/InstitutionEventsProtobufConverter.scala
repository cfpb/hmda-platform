package hmda.persistence.serialization.institution

import InstitutionProtobufConverter._
import hmda.persistence.institution.InstitutionPersistence.{
  InstitutionCreated,
  InstitutionDeleted,
  InstitutionModified
}
import hmda.persistence.serialization.institution.events.{
  InstitutionCreatedMessage,
  InstitutionDeletedMessage,
  InstitutionModifiedMessage
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
      i = institutionFromProtobuf(msg.institution)
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
      i = institutionFromProtobuf(msg.institution)
    )
  }

  def institutionDeletedToProtobuf(
      evt: InstitutionDeleted): InstitutionDeletedMessage = ???

  def institutionDeletedFromProtobuf(
      msg: InstitutionDeletedMessage): InstitutionDeleted = ???
}
