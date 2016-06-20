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

  private val smallCounties = cbsaTracts
    .filter { cbsa => cbsa.smallCounty == 1 }
    .map { cbsa => (cbsa.state, cbsa.county) }
    .toSet

  implicit def smallCounty: Predicate[Geography] = new Predicate[Geography] {
    override def validate: (Geography) => Boolean = (geo) =>
      smallCounties.contains((geo.state, geo.county))
  }

  implicit def validStateCountyCombination: Predicate[Geography] = new Predicate[Geography] {
    override def validate: (Geography) => Boolean = (geo) =>
      validStateCountyCombinationSet.contains((geo.state, geo.county))
  }

  implicit def validStateCountyTractCombination: Predicate[Geography] = new Predicate[Geography] {
    override def validate: (Geography) => Boolean = (geo) =>
      validStateCountyTractCombinationSet.contains((geo.state, geo.county, geo.tract))
  }

  implicit def validCompleteCombination: Predicate[Geography] = new Predicate[Geography] {
    override def validate: (Geography) => Boolean = (geo) =>
      validMsaCombinationSet.contains((geo.msa, geo.state, geo.county, geo.tract)) ||
        validMdCombinationSet.contains((geo.msa, geo.state, geo.county, geo.tract))
  }

  implicit def validStateCountyMsaCombination: Predicate[Geography] = new Predicate[Geography] {
    override def validate: (Geography) => Boolean = (geo) =>
      validMsaCombinationSetNoTract.contains((geo.msa, geo.state, geo.county)) ||
        validMdCombinationSetNoTract.contains((geo.msa, geo.state, geo.county))
  }

  implicit def stateCountyCombinationInMsaNotMicro: Predicate[Geography] = new Predicate[Geography] {
    override def validate: (Geography) => Boolean = (geo) =>
      hasMsaNotMicroSet.contains((geo.state, geo.county))
  }

}
