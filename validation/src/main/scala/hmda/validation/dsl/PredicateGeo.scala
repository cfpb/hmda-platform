package hmda.validation.dsl

import hmda.model.census.{ CBSAMetroMicroLookup, CBSATractLookup }
import hmda.model.fi.lar.Geography

object PredicateGeo {
  val cbsaTracts = CBSATractLookup.values

  val cbsaMetroMicro = CBSAMetroMicroLookup.values

  val validMsaCombinationSet = cbsaTracts.map { cbsa =>
    (cbsa.geoIdMsa, cbsa.state, cbsa.county, cbsa.tractDecimal)
  }.toSet

  val validMdCombinationSet = cbsaTracts.map { cbsa =>
    (cbsa.metDivFp, cbsa.state, cbsa.county, cbsa.tractDecimal)
  }.toSet

  val validMsaCombinationSetNoTract = cbsaTracts.map { cbsa =>
    (cbsa.geoIdMsa, cbsa.state, cbsa.county)
  }.toSet

  val validMdCombinationSetNoTract = cbsaTracts.map { cbsa =>
    (cbsa.metDivFp, cbsa.state, cbsa.county)
  }.toSet

  private val MsaNotMicro = cbsaMetroMicro
    .filter(_.MEMI == 1)
    .map { _.GEOIOD }

  val hasMsaNotMicroSet = cbsaTracts
    .filter(cbsa => MsaNotMicro.contains(cbsa.geoIdMsa))
    .map(cbsa => (cbsa.state, cbsa.county))
    .toSet

  val validStateCountyTractCombinationSet = cbsaTracts
    .map { cbsa => (cbsa.state, cbsa.county, cbsa.tractDecimal)}
    .toSet

  val validStateCountyCombinationSet = cbsaTracts.map { cbsa =>
    (cbsa.state, cbsa.county)
  }.toSet

  val smallCounties = cbsaTracts
    .filter { cbsa => cbsa.smallCounty == 1 }
    .map { cbsa => (cbsa.state, cbsa.county) }
    .toSet

  implicit def smallCounty: Predicate[Geography] = new Predicate[Geography] {
    override def validate: (Geography) => Boolean = _.asInstanceOf[AnyRef] match {
      case geo: Geography => smallCounties.contains((geo.state, geo.county))
      case _ => false
    }
    override def failure: String = "county is not small"
  }

  implicit def validStateCountyCombination: Predicate[Geography] = new Predicate[Geography] {
    override def validate: (Geography) => Boolean = _.asInstanceOf[AnyRef] match {
      case geo: Geography => validStateCountyCombinationSet.contains((geo.state, geo.county))
      case _ => false
    }
    override def failure: String = "state and county combination is not valid"
  }

  implicit def validStateCountyTractCombination: Predicate[Geography] = new Predicate[Geography] {
    override def validate: (Geography) => Boolean = _.asInstanceOf[AnyRef] match {
      case geo: Geography => validStateCountyTractCombinationSet.contains((geo.state, geo.county, geo.tract))
      case _ => false
    }
    override def failure: String = "state, county, and census tract combination is not valid"
  }

  implicit def validCompleteCombination: Predicate[Geography] = new Predicate[Geography] {
    override def validate: (Geography) => Boolean = _.asInstanceOf[AnyRef] match {
      case geo: Geography => validMsaCombinationSet.contains((geo.msa, geo.state, geo.county, geo.tract)) ||
        validMdCombinationSet.contains((geo.msa, geo.state, geo.county, geo.tract))
      case _ => false
    }
    override def failure: String = "state, county, msa, and census tract combination is not valid"
  }

  implicit def validStateCountyMsaCombination: Predicate[Geography] = new Predicate[Geography] {
    override def validate: (Geography) => Boolean = _.asInstanceOf[AnyRef] match {
      case geo: Geography => validMsaCombinationSetNoTract.contains((geo.msa, geo.state, geo.county)) ||
        validMdCombinationSetNoTract.contains((geo.msa, geo.state, geo.county))
      case _ => false
    }
    override def failure: String = "state, county, msa, and census tract combination is not valid"
  }

  implicit def shouldHaveMsa: Predicate[Geography] = new Predicate[Geography] {
    override def validate: (Geography) => Boolean = _.asInstanceOf[AnyRef] match {
      case geo: Geography => hasMsaNotMicroSet.contains((geo.state, geo.county))
      case _ => false
    }
    override def failure: String = "state, county, msa, and census tract combination is not valid"
  }

}
