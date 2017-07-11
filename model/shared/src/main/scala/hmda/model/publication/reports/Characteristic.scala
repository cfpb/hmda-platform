package hmda.model.publication.reports

sealed trait Characteristic

case class RaceCharacteristic(race: RaceEnum, dispositions: List[Disposition]) extends Characteristic

case class EthnicityCharacteristic(ethnicity: EthnicityEnum, dispositions: List[Disposition]) extends Characteristic

case class MinorityStatusCharacteristic(minorityStatus: MinorityStatusEnum, dispositions: List[Disposition]) extends Characteristic
