package hmda.model.publication.reports

sealed trait BorrowerCharacteristic

case class RaceBorrowerCharacteristic(races: List[RaceCharacteristic]) extends BorrowerCharacteristic

case class EthnicityBorrowerCharacteristic(ethnicities: List[EthnicityCharacteristic]) extends BorrowerCharacteristic

case class MinorityStatusBorrowerCharacteristic(minoritystatus: List[MinorityStatusCharacteristic]) extends BorrowerCharacteristic
