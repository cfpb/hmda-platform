package hmda.persistence.serialization.apor

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import hmda.model.apor.{ APOR, FixedRate, RateType, VariableRate }
import hmda.persistence.messages.commands.apor.APORCommands.CreateApor
import hmda.persistence.messages.events.apor.APOREvents._
import hmda.persistence.model.serialization.APOR.{ APORMessage, RateTypeMessage }
import hmda.persistence.model.serialization.APORCommands.CreateAPORMessage
import hmda.persistence.model.serialization.APOREvents._

object APORProtobufConverter {

  def aporToProtobuf(obj: APOR): APORMessage = {
    APORMessage(
      loanTerm = obj.loanTerm.toString,
      values = obj.values
    )
  }

  def aporFromProtobuf(msg: APORMessage): APOR = {
    APOR(
      loanTerm = LocalDate.parse(msg.loanTerm, DateTimeFormatter.ISO_LOCAL_DATE),
      values = msg.values
    )
  }

  def rateTypeToProtobuf(obj: RateType): RateTypeMessage = {
    obj match {
      case FixedRate => RateTypeMessage.FIXED
      case VariableRate => RateTypeMessage.VARIABLE
    }
  }

  def rateTypeFromProtobuf(msg: RateTypeMessage): RateType = {
    msg match {
      case RateTypeMessage.FIXED => FixedRate
      case RateTypeMessage.VARIABLE => VariableRate
      case _ => throw new RuntimeException("Cannot convert this rate type to protobuf")
    }
  }

  def createAporToProtobuf(obj: CreateApor): CreateAPORMessage = {
    CreateAPORMessage(
      apor = Some(aporToProtobuf(obj.apor)),
      rateType = rateTypeToProtobuf(obj.rateType)
    )
  }

  def createAporFromProtobuf(msg: CreateAPORMessage): CreateApor = {
    CreateApor(
      aporFromProtobuf(msg.apor.getOrElse(APORMessage())),
      rateType = rateTypeFromProtobuf(msg.rateType)
    )
  }

  def aporCreatedToProtobuf(obj: AporCreated): APORCreatedMessage = {
    APORCreatedMessage(
      apor = Some(aporToProtobuf(obj.apor)),
      rateType = rateTypeToProtobuf(obj.rateType)
    )
  }

  def aporCreatedFromProtobuf(msg: APORCreatedMessage): AporCreated = {
    AporCreated(
      aporFromProtobuf(msg.apor.getOrElse(APORMessage())),
      rateType = rateTypeFromProtobuf(msg.rateType)
    )
  }

}
