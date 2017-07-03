package hmda.publication.reports.protocol

import hmda.model.publication.reports._
import spray.json._

trait BorrowerCharacteristicProtocol
    extends DefaultJsonProtocol
    with RaceEnumProtocol
    with EthnicityEnumProtocol
    with MinorityStatusEnumProtocol
    with DispositionProtocol {

  implicit object BorrowerCharacteristicFormat extends RootJsonFormat[BorrowerCharacteristic] {

    override def write(obj: BorrowerCharacteristic): JsValue = obj match {
      case RaceCharacteristic(race, dispositions) =>
        JsObject(
          "race" -> JsString(race.description),
          "dispositions" -> dispositions.toJson
        )
      case EthnicityCharacteristic(ethnicity, dispositions) =>
        JsObject(
          "ethnicity" -> JsString(ethnicity.description),
          "dispositions" -> dispositions.toJson
        )
      case MinorityCharacteristic(minorityStatus, dispositions) =>
        JsObject(
          "minorityStatus" -> JsString(minorityStatus.description),
          "dispositions" -> dispositions.toJson
        )

    }

    override def read(json: JsValue): BorrowerCharacteristic = json match {
      case JsObject(fields) if fields.isDefinedAt("race") =>
        RaceCharacteristic(
          fields("race").convertTo[RaceEnum],
          fields("dispositions").convertTo[List[Disposition]]
        )
      case JsObject(fields) if fields.isDefinedAt("ethnicity") =>
        EthnicityCharacteristic(
          fields("ethnicity").convertTo[EthnicityEnum],
          fields("dispositions").convertTo[List[Disposition]]
        )
      case JsObject(fields) if fields.isDefinedAt("minorityStatus") =>
        MinorityCharacteristic(
          fields("minorityStatus").convertTo[MinorityStatusEnum],
          fields("dispositions").convertTo[List[Disposition]]
        )
    }

  }
}
