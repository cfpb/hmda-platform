package hmda.serialization.institution

import hmda.messages.institution.InstitutionEvents._
import hmda.persistence.serialization.filing.FilingMessage
import hmda.persistence.serialization.institution.InstitutionMessage
import hmda.persistence.serialization.institution.events.InstitutionKafkaEventMessage.InstitutionEventField
import hmda.persistence.serialization.institution.events.InstitutionKafkaEventMessage.InstitutionEventField.{
  InstitutionCreatedField,
  InstitutionDeletedField,
  InstitutionModifiedField,
  InstitutionWithLouField
}
import hmda.persistence.serialization.institution.events._
import hmda.serialization.filing.FilingProtobufConverter._
import hmda.serialization.institution.InstitutionProtobufConverter._
// $COVERAGE-OFF$
object InstitutionEventsProtobufConverter {

  def institutionCreatedToProtobuf(evt: InstitutionCreated): InstitutionCreatedMessage =
    InstitutionCreatedMessage(
      institution = Some(institutionToProtobuf(evt.i))
    )

  def institutionCreatedFromProtobuf(msg: InstitutionCreatedMessage): InstitutionCreated =
    InstitutionCreated(
      i = institutionFromProtobuf(msg.institution.getOrElse(InstitutionMessage()))
    )

  def institutionWithLouToProtobuf(evt: InstitutionWithLou): InstitutionWithLouMessage =
    InstitutionWithLouMessage(
      institution = Some(institutionToProtobuf(evt.i))
    )

  def institutionWithLouFromProtobuf(msg: InstitutionWithLouMessage): InstitutionWithLou =
    InstitutionWithLou(
      i = institutionFromProtobuf(msg.institution.getOrElse(InstitutionMessage()))
    )

  def institutionModifiedToProtobuf(evt: InstitutionModified): InstitutionModifiedMessage =
    InstitutionModifiedMessage(
      institution = Some(institutionToProtobuf(evt.i))
    )

  def institutionModifiedFromProtobuf(msg: InstitutionModifiedMessage): InstitutionModified =
    InstitutionModified(
      i = institutionFromProtobuf(msg.institution.getOrElse(InstitutionMessage()))
    )

  def institutionDeletedToProtobuf(evt: InstitutionDeleted): InstitutionDeletedMessage =
    InstitutionDeletedMessage(
      lei = evt.LEI,
      year = evt.year
    )

  def institutionDeletedFromProtobuf(msg: InstitutionDeletedMessage): InstitutionDeleted =
    InstitutionDeleted(
      LEI = msg.lei,
      year = msg.year
    )

  def institutionNotExistsToProtobuf(evt: InstitutionNotExists): InstitutionNotExistsMessage =
    InstitutionNotExistsMessage(
      lei = evt.LEI
    )

  def institutionNotExistsFromProtobuf(msg: InstitutionNotExistsMessage): InstitutionNotExists =
    InstitutionNotExists(
      LEI = msg.lei
    )

  def filingAddedToProtobuf(evt: FilingAdded): FilingAddedMessage =
    FilingAddedMessage(
      if (evt.filing.isEmpty) None
      else Some(filingToProtobuf(evt.filing))
    )

  def filingAddedFromProtobuf(msg: FilingAddedMessage): FilingAdded =
    FilingAdded(filingFromProtobuf(msg.filing.getOrElse(FilingMessage())))

  def institutionKafkaEventToProtobuf(evt: InstitutionKafkaEvent): InstitutionKafkaEventMessage =
    evt.institutionEvent match {
      case ic: InstitutionCreated =>
        val field = InstitutionCreatedField(institutionCreatedToProtobuf(ic))
        InstitutionKafkaEventMessage(evt.eventType, field)
      case il: InstitutionWithLou =>
        val field = InstitutionWithLouField(institutionWithLouToProtobuf(il))
        InstitutionKafkaEventMessage(evt.eventType, field)
      case im: InstitutionModified =>
        val field = InstitutionModifiedField(institutionModifiedToProtobuf(im))
        InstitutionKafkaEventMessage(evt.eventType, field)
      case id: InstitutionDeleted =>
        val field = InstitutionDeletedField(institutionDeletedToProtobuf(id))
        InstitutionKafkaEventMessage(evt.eventType, field)

      case other =>
        sys.error(s"Unexpected $other when invoking institutionKafkaEventToProtobuf")
    }

  def institutionKafkaEventFromProtobuf(bytes: Array[Byte]): InstitutionKafkaEvent = {
    val msg = InstitutionKafkaEventMessage.parseFrom(bytes)
    msg.institutionEventField match {
      case InstitutionEventField.InstitutionCreatedField(ic) =>
        InstitutionKafkaEvent(msg.eventType, institutionCreatedFromProtobuf(ic))
      case InstitutionEventField.InstitutionWithLouField(ic) =>
        InstitutionKafkaEvent(msg.eventType, institutionWithLouFromProtobuf(ic))
      case InstitutionEventField.InstitutionModifiedField(im) =>
        InstitutionKafkaEvent(msg.eventType, institutionModifiedFromProtobuf(im))
      case InstitutionEventField.InstitutionDeletedField(id) =>
        InstitutionKafkaEvent(msg.eventType, institutionDeletedFromProtobuf(id))

      case other =>
        sys.error(s"Unexpected $other when invoking institutionKafkaEventFromProtobuf")
    }
  }

}
// $COVERAGE-OFF$