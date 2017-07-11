package hmda.publication.reports.protocol

import hmda.model.publication.reports._
import spray.json._

trait BorrowerCharacteristicProtocol extends CharacteristicProtocol {

  implicit object BorrowerCharacteristicFormat extends RootJsonFormat[BorrowerCharacteristic] {
    override def write(obj: BorrowerCharacteristic): JsValue = obj match {

      case RaceBorrowerCharacteristic(races) =>
        JsObject(
          "characteristic" -> JsString("Race"),
          "races" -> races.toJson
        )

      case EthnicityBorrowerCharacteristic(ethnicities) =>
        JsObject(
          "characteristic" -> JsString("Ethnicity"),
          "ethnicities" -> ethnicities.toJson
        )

      case MinorityStatusBorrowerCharacteristic(minoritystatus) =>
        JsObject(
          "characteristic" -> JsString("Minority Status"),
          "minoritystatus" -> minoritystatus.toJson
        )

    }

    override def read(json: JsValue): BorrowerCharacteristic = json match {
      case JsObject(fields) if fields.isDefinedAt("races") =>
        RaceBorrowerCharacteristic(
          fields("races").convertTo[List[RaceCharacteristic]]
        )

      case JsObject(fields) if fields.isDefinedAt("ethnicities") =>
        EthnicityBorrowerCharacteristic(
          fields("ethnicities").convertTo[List[EthnicityCharacteristic]]
        )

      case JsObject(fields) if fields.isDefinedAt("minoritystatus") =>
        MinorityStatusBorrowerCharacteristic(
          fields("minoritystatus").convertTo[List[MinorityStatusCharacteristic]]
        )
    }

  }
}
