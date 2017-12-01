package hmda.persistence.messages.commands.apor

import hmda.model.apor.{ APOR, RateType }
import hmda.persistence.messages.CommonMessages.Command

object APORCommands {
  case class CreateApor(apor: APOR, rateType: RateType) extends Command
}
