package hmda.model.publication.reports

sealed trait BorrowerCharacteristic

case class RaceCharacteristic(race: RaceEnum, dispositions: List[Disposition]) extends BorrowerCharacteristic

case class EthnicityCharacteristic(ethnicity: EthnicityEnum, dispositions: List[Disposition]) extends BorrowerCharacteristic

case class MinorityCharacteristic(minorityStatus: MinorityStatusEnum, dispositions: List[Disposition]) extends BorrowerCharacteristic
