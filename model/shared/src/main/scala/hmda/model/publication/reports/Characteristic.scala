package hmda.model.publication.reports

sealed trait Characteristic {
  def dispositions: List[ValueDisposition]
}

case class RaceCharacteristic(race: RaceEnum, dispositions: List[ValueDisposition]) extends Characteristic {
  def +(rc: RaceCharacteristic) = {
    val combined = dispositions.map(d =>
      d + rc.dispositions.find(_.dispositionName == d.dispositionName).get)
    RaceCharacteristic(race, combined)
  }
}

case class EthnicityCharacteristic(ethnicity: EthnicityEnum, dispositions: List[ValueDisposition]) extends Characteristic {
  def +(ec: EthnicityCharacteristic) = {
    val combined = dispositions.map(d =>
      d + ec.dispositions.find(_.dispositionName == d.dispositionName).get)
    EthnicityCharacteristic(ethnicity, combined)
  }
}

case class MinorityStatusCharacteristic(minorityStatus: MinorityStatusEnum, dispositions: List[ValueDisposition]) extends Characteristic {
  def +(msc: MinorityStatusCharacteristic) = {
    val combined = dispositions.map(d =>
      d + msc.dispositions.find(_.dispositionName == d.dispositionName).get)
    MinorityStatusCharacteristic(minorityStatus, combined)
  }
}
