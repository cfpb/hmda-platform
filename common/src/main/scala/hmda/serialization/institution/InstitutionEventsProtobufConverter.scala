package hmda.serialization.institution

import hmda.messages.institution.InstitutionEvents._
import hmda.persistence.serialization.institution.InstitutionMessage
import hmda.persistence.serialization.institution.events._
import InstitutionProtobufConverter._
import hmda.persistence.serialization.filing.FilingMessage
import hmda.serialization.filing.FilingProtobufConverter._

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
      lei = evt.LEI
    )
  }

  def institutionDeletedFromProtobuf(
      msg: InstitutionDeletedMessage): InstitutionDeleted = {
    InstitutionDeleted(
      LEI = msg.lei
    )
  }

  def institutionNotExistsToProtobuf(
      evt: InstitutionNotExists): InstitutionNotExistsMessage = {
    InstitutionNotExistsMessage(
      lei = evt.LEI
    )
  }

  def institutionNotExistsFromProtobuf(
      msg: InstitutionNotExistsMessage): InstitutionNotExists = {
    InstitutionNotExists(
      LEI = msg.lei
    )
  }

  def filingAddedToProtobuf(evt: FilingAdded): FilingAddedMessage = {
    FilingAddedMessage(
      if (evt.filing.isEmpty) None
      else Some(filingToProtobuf(evt.filing))
    )
  }

  def filingAddedFromProtobuf(msg: FilingAddedMessage): FilingAdded = {
    FilingAdded(filingFromProtobuf(msg.filing.getOrElse(FilingMessage())))
  }

}
