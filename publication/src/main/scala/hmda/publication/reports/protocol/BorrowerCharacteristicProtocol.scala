package hmda.publication.reports.protocol

import hmda.model.publication.reports._
import spray.json._

trait BorrowerCharacteristicProtocol extends CharacteristicProtocol {

  implicit object RaceBorrowerCharacteristicFormat extends RootJsonFormat[RaceBorrowerCharacteristic] {
    override def write(obj: RaceBorrowerCharacteristic): JsValue =
      JsObject(
        "characteristic" -> JsString("Race"),
        "races" -> obj.toJson
      )

    override def read(json: JsValue): RaceBorrowerCharacteristic = json match {
      case JsObject(fields) if fields.isDefinedAt("races") =>
        RaceBorrowerCharacteristic(
          fields("races").convertTo[List[RaceCharacteristic]]
        )
    }
  }

  implicit object EthnicityBorrowerCharacteristicFormat extends RootJsonFormat[EthnicityBorrowerCharacteristic] {
    override def write(obj: EthnicityBorrowerCharacteristic): JsValue =
      JsObject(
        "characteristic" -> JsString("Ethnicity"),
        "ethnicities" -> obj.toJson
      )

    override def read(json: JsValue): EthnicityBorrowerCharacteristic = json match {
      case JsObject(fields) if fields.isDefinedAt("ethnicities") =>
        EthnicityBorrowerCharacteristic(
          fields("ethnicities").convertTo[List[EthnicityCharacteristic]]
        )
    }
  }

  implicit object MinorityStatusBorrowerCharacteristicFormat extends RootJsonFormat[MinorityStatusBorrowerCharacteristic] {
    override def write(obj: MinorityStatusBorrowerCharacteristic): JsValue =
      JsObject(
        "characteristic" -> JsString("Minority Status"),
        "minorityStatus" -> obj.toJson
      )

    override def read(json: JsValue): MinorityStatusBorrowerCharacteristic = json match {
      case JsObject(fields) if fields.isDefinedAt("minorityStatus") =>
        MinorityStatusBorrowerCharacteristic(
          fields("minorityStatus").convertTo[List[MinorityStatusCharacteristic]]
        )
    }
  }
}
