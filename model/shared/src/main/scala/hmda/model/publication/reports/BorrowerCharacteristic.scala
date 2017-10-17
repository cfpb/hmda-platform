package hmda.model.publication.reports

sealed trait BorrowerCharacteristic

case class RaceBorrowerCharacteristic(races: List[RaceCharacteristic]) extends BorrowerCharacteristic {
  def +(rbc: RaceBorrowerCharacteristic) = {
    val combined = races.map(r =>
      r + rbc.races.find(_.race == r.race).get)
    RaceBorrowerCharacteristic(combined)
  }
}

case class EthnicityBorrowerCharacteristic(ethnicities: List[EthnicityCharacteristic]) extends BorrowerCharacteristic {
  def +(ebc: EthnicityBorrowerCharacteristic) = {
    val combined = ethnicities.map(e =>
      e + ebc.ethnicities.find(_.ethnicity == e.ethnicity).get)
    EthnicityBorrowerCharacteristic(combined)
  }
}

case class MinorityStatusBorrowerCharacteristic(minoritystatus: List[MinorityStatusCharacteristic]) extends BorrowerCharacteristic {
  def +(msbc: MinorityStatusBorrowerCharacteristic) = {
    val combined = minoritystatus.map(m =>
      m + msbc.minoritystatus.find(_.minorityStatus == m.minorityStatus).get)
    MinorityStatusBorrowerCharacteristic(combined)
  }
}
