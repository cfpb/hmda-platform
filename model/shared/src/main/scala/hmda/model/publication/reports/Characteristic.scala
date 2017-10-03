package hmda.model.publication.reports

sealed trait Characteristic {
  def dispositions: List[Disposition]
}

case class RaceCharacteristic(race: RaceEnum, dispositions: List[Disposition]) extends Characteristic {
  def +(rc: RaceCharacteristic) = {
    val combined = dispositions.map(d =>
      d + rc.dispositions.find(_.disposition == d.disposition).get)
    RaceCharacteristic(race, combined)
  }
}

case class EthnicityCharacteristic(ethnicity: EthnicityEnum, dispositions: List[Disposition]) extends Characteristic {
  def +(ec: EthnicityCharacteristic) = {
    val combined = dispositions.map(d =>
      d + ec.dispositions.find(_.disposition == d.disposition).get)
    EthnicityCharacteristic(ethnicity, combined)
  }
}


case class MinorityStatusCharacteristic(minorityStatus: MinorityStatusEnum, dispositions: List[Disposition]) extends Characteristic {
  def +(msc: MinorityStatusCharacteristic) = {
    val combined = dispositions.map(d =>
      d + msc.dispositions.find(_.disposition == d.disposition).get)
    MinorityStatusCharacteristic(minorityStatus, combined)
  }
}
