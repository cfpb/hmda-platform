package hmda.publication.reports.protocol

import hmda.model.publication.reports.{ BorrowerCharacteristic, EthnicityBorrowerCharacteristic, MinorityStatusBorrowerCharacteristic, RaceBorrowerCharacteristic }
import spray.json._

class BorrowerCharacteristicProtocol extends CharacteristicProtocol {

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
          "ethnicities" -> minoritystatus.toJson
        )

    }

    override def read(json: JsValue): BorrowerCharacteristic = ???

  }
}
