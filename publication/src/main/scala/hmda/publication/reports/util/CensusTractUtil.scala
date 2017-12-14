package hmda.publication.reports.util

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.census.model.{ Tract, TractLookup }
import hmda.model.fi.lar.LoanApplicationRegister

object CensusTractUtil {

  def filterMinorityPopulation(lars: Source[LoanApplicationRegister, NotUsed], lower: Double, upper: Double, tracts: Set[Tract]): Source[LoanApplicationRegister, NotUsed] = {
    lars.filter { lar =>
      TractLookup.forLar(lar, tracts) match {
        case Some(tract) =>
          val minorityPop = tract.minorityPopulationPercent
          minorityPop >= lower && minorityPop < upper
        case _ => false
      }
    }
  }

  def filterIncomeCharacteristics(lars: Source[LoanApplicationRegister, NotUsed], lower: Double, upper: Double, tracts: Set[Tract]): Source[LoanApplicationRegister, NotUsed] = {
    lars.filter { lar =>
      TractLookup.forLar(lar, tracts) match {
        case Some(tract) =>
          val mfiRatio = tract.tractMfiPercentageOfMsaMfi
          mfiRatio >= lower && mfiRatio < upper
        case _ => false
      }
    }
  }

}
