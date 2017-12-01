package hmda.persistence.serialization.apor

import hmda.model.apor.APORGenerator.{ localDateGen, rateTypeGen }
import hmda.persistence.messages.commands.apor.APORCommands.CalculateRateSpread
import org.scalacheck.Gen

object CalculateRateSpreadGenerator {
  def calculateRateSpreadGen: Gen[CalculateRateSpread] = {
    for {
      actionTakenType <- Gen.oneOf(1, 2, 8)
      amortizationType <- Gen.choose(1, 50)
      rateType <- rateTypeGen
      apr <- Gen.choose(0, Double.MaxValue)
      lockinDate <- localDateGen
      reverseMortgage <- Gen.oneOf(1, 2)
    } yield CalculateRateSpread(
      actionTakenType,
      amortizationType,
      rateType,
      apr,
      lockinDate,
      reverseMortgage
    )
  }
}
