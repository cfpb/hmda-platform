package hmda.publication.reports.util

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.census.model.{ Tract, TractLookup }
import hmda.model.census.CBSATractLookup
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

  def filterMedianYearHomesBuilt(lars: Source[LoanApplicationRegister, NotUsed], lower: Int, upper: Int, tracts: Set[Tract]): Source[LoanApplicationRegister, NotUsed] = {
    lars.filter { lar =>
      TractLookup.forLar(lar, tracts) match {
        case Some(tract) =>
          tract.medianYearHomesBuilt match {
            case Some(medianAge) => medianAge >= lower && medianAge < upper
            case _ => false
          }
        case _ => false
      }
    }
  }

  def filterUnknownMedianYearBuilt(lars: Source[LoanApplicationRegister, NotUsed], tracts: Set[Tract]): Source[LoanApplicationRegister, NotUsed] = {
    lars.filter { lar =>
      TractLookup.forLar(lar, tracts) match {
        case Some(tract) =>
          tract.medianYearHomesBuilt match {
            case None => true
            case _ => false
          }
        case _ => true
      }
    }
  }

  def filterSmallCounty(lars: Source[LoanApplicationRegister, NotUsed]): Source[LoanApplicationRegister, NotUsed] = {
    lars.filter(lar => CBSATractLookup.geoIsSmallCounty(lar.geography))
  }

  def filterNotSmallCounty(lars: Source[LoanApplicationRegister, NotUsed]): Source[LoanApplicationRegister, NotUsed] = {
    lars.filterNot(lar => CBSATractLookup.geoIsSmallCounty(lar.geography))
  }

}
