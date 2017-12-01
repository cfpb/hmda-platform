package hmda.persistence.messages.commands.apor

import java.time.LocalDate

import hmda.model.apor.{ APOR, RateType }
import hmda.persistence.messages.CommonMessages.Command

object APORCommands {
  case class CreateApor(apor: APOR, rateType: RateType) extends Command
  case class CalculateRateSpread(
    actionTakenType: Int,
    amortizationType: Int,
    rateType: RateType,
    apr: Double,
    lockinDate: LocalDate,
    reverseMortgage: Int
  )
}
