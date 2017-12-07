package hmda.publication.reports.protocol

import hmda.model.publication.reports._
import spray.json._

trait CharacteristicProtocol
    extends RaceEnumProtocol
    with EthnicityEnumProtocol
    with MinorityStatusEnumProtocol
    with DispositionProtocol {

  implicit object RaceCharacteristicFormat extends RootJsonFormat[RaceCharacteristic] {

    override def write(obj: RaceCharacteristic): JsValue = {
      JsObject(
        "race" -> JsString(obj.race.description),
        "dispositions" -> obj.dispositions.toJson
      )
    }

    override def read(json: JsValue): RaceCharacteristic = json match {
      case JsObject(fields) =>
        RaceCharacteristic(
          fields("race").convertTo[RaceEnum],
          fields("dispositions").convertTo[List[ValueDisposition]]
        )
      case _ => throw DeserializationException("Cannot deserialize JSON structure")
    }

  }

  implicit object EthnicityCharacteristicFormat extends RootJsonFormat[EthnicityCharacteristic] {

    override def write(obj: EthnicityCharacteristic): JsValue = {
      JsObject(
        "ethnicity" -> JsString(obj.ethnicity.description),
        "dispositions" -> obj.dispositions.toJson
      )
    }

    override def read(json: JsValue): EthnicityCharacteristic = json match {
      case JsObject(fields) =>
        EthnicityCharacteristic(
          fields("ethnicity").convertTo[EthnicityEnum],
          fields("dispositions").convertTo[List[ValueDisposition]]
        )
      case _ => throw DeserializationException("Cannot deserialize JSON structure")
    }

  }

  implicit object MinorityStatusCharacteristicFormat extends RootJsonFormat[MinorityStatusCharacteristic] {

    override def write(obj: MinorityStatusCharacteristic): JsValue = {
      JsObject(
        "minorityStatus" -> JsString(obj.minorityStatus.description),
        "dispositions" -> obj.dispositions.toJson
      )
    }

    override def read(json: JsValue): MinorityStatusCharacteristic = json match {
      case JsObject(fields) =>
        MinorityStatusCharacteristic(
          fields("minorityStatus").convertTo[MinorityStatusEnum],
          fields("dispositions").convertTo[List[ValueDisposition]]
        )
      case _ => throw DeserializationException("Cannot deserialize JSON structure")
    }

  }

  implicit object CharacteristicFormat extends RootJsonFormat[Characteristic] {

    override def write(obj: Characteristic): JsValue = obj match {
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
      case MinorityStatusCharacteristic(minorityStatus, dispositions) =>
        JsObject(
          "minorityStatus" -> JsString(minorityStatus.description),
          "dispositions" -> dispositions.toJson
        )
    }

    override def read(json: JsValue): Characteristic = json match {
      case JsObject(fields) if fields.isDefinedAt("race") =>
        RaceCharacteristic(
          fields("race").convertTo[RaceEnum],
          fields("dispositions").convertTo[List[ValueDisposition]]
        )
      case JsObject(fields) if fields.isDefinedAt("ethnicity") =>
        EthnicityCharacteristic(
          fields("ethnicity").convertTo[EthnicityEnum],
          fields("dispositions").convertTo[List[ValueDisposition]]
        )
      case JsObject(fields) if fields.isDefinedAt("minorityStatus") =>
        MinorityStatusCharacteristic(
          fields("minorityStatus").convertTo[MinorityStatusEnum],
          fields("dispositions").convertTo[List[ValueDisposition]]
        )
    }

  }

}
