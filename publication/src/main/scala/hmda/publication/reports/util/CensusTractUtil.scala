package hmda.publication.reports.util

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.census.model.TractLookup
import hmda.model.fi.lar.LoanApplicationRegister

object CensusTractUtil {

  def filterMinorityPopulation(lars: Source[LoanApplicationRegister, NotUsed], lower: Double, upper: Double): Source[LoanApplicationRegister, NotUsed] = {
    lars.filter { lar =>
      TractLookup.forLar(lar) match {
        case Some(tract) =>
          val minorityPop = tract.minorityPopulationPercent
          minorityPop >= lower && minorityPop < upper
        case _ => false
      }
    }
  }

  def filterIncomeCharacteristics(lars: Source[LoanApplicationRegister, NotUsed], lower: Double, upper: Double): Source[LoanApplicationRegister, NotUsed] = {
    lars.filter { lar =>
      TractLookup.forLar(lar) match {
        case Some(tract) =>
          val mfiRatio = tract.tractMfiPercentageOfMsaMfi
          mfiRatio >= lower && mfiRatio < upper
        case _ => false
      }
    }
  }

}
