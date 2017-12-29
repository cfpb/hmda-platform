package hmda.persistence.serialization.apor

import hmda.model.apor.APORGenerator.{ localDateGen, rateTypeGen }
import hmda.persistence.messages.commands.apor.APORCommands.CalculateRateSpread
import org.scalacheck.Gen

trait CalculateRateSpreadGenerator {
  implicit def calculateRateSpreadGen: Gen[CalculateRateSpread] = {
    for {
      actionTakenType <- Gen.oneOf(1, 2, 8)
      loanTerm <- Gen.choose(1, 50)
      amortizationType <- rateTypeGen
      apr <- Gen.choose(0, Double.MaxValue)
      lockInDate <- localDateGen
      reverseMortgage <- Gen.oneOf(1, 2)
    } yield CalculateRateSpread(
      actionTakenType,
      loanTerm,
      amortizationType,
      apr,
      lockInDate,
      reverseMortgage
    )
  }
}
