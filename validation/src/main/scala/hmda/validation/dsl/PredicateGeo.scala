package hmda.validation.dsl

import hmda.model.census.{ CBSAMetroMicroLookup, CBSATractLookup }
import hmda.model.fi.lar.Geography

object PredicateGeo {
  private val cbsaTracts = CBSATractLookup.values

  private val cbsaMetroMicro = CBSAMetroMicroLookup.values

  private val validMsaCombinationSet = cbsaTracts.map { cbsa =>
    (cbsa.geoIdMsa, cbsa.state, cbsa.county, cbsa.tractDecimal)
  }.toSet

  private val validMdCombinationSet = cbsaTracts.map { cbsa =>
    (cbsa.metDivFp, cbsa.state, cbsa.county, cbsa.tractDecimal)
  }.toSet

  private val validMsaCombinationSetNoTract = cbsaTracts.map { cbsa =>
    (cbsa.geoIdMsa, cbsa.state, cbsa.county)
  }.toSet

  private val validMdCombinationSetNoTract = cbsaTracts.map { cbsa =>
    (cbsa.metDivFp, cbsa.state, cbsa.county)
  }.toSet

  private val msaNotMicro = cbsaMetroMicro
    .filter(_.metroMicro == 1)
    .map { _.geoId }
    .toSet

  private val hasMsaNotMicroSet = cbsaTracts
    .filter(cbsa => msaNotMicro.contains(cbsa.geoIdMsa))
    .map(cbsa => (cbsa.state, cbsa.county))
    .toSet

  private val validStateCountyTractCombinationSet = cbsaTracts
    .map { cbsa => (cbsa.state, cbsa.county, cbsa.tractDecimal) }
    .toSet

  private val validStateCountyCombinationSet = cbsaTracts.map { cbsa =>
    (cbsa.state, cbsa.county)
  }.toSet

  def smallCounty: Predicate[Geography] = { (geo: Geography) =>
    CBSATractLookup.smallCounties.contains((geo.state, geo.county))
  }

  def validStateCountyCombination: Predicate[Geography] = { (geo: Geography) =>
    validStateCountyCombinationSet.contains((geo.state, geo.county))
  }

  def validStateCountyTractCombination: Predicate[Geography] = { (geo: Geography) =>
    validStateCountyTractCombinationSet.contains((geo.state, geo.county, geo.tract))
  }

  def validCompleteCombination: Predicate[Geography] = { (geo: Geography) =>
    validMsaCombinationSet.contains((geo.msa, geo.state, geo.county, geo.tract)) ||
      validMdCombinationSet.contains((geo.msa, geo.state, geo.county, geo.tract))
  }

  def validStateCountyMsaCombination: Predicate[Geography] = { (geo: Geography) =>
    validMsaCombinationSetNoTract.contains((geo.msa, geo.state, geo.county)) ||
      validMdCombinationSetNoTract.contains((geo.msa, geo.state, geo.county))
  }

  def stateCountyCombinationInMsaNotMicro: Predicate[Geography] = { (geo: Geography) =>
    hasMsaNotMicroSet.contains((geo.state, geo.county))
  }

}
