package hmda.api.protocol.apor

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import hmda.api.model.public.RateSpreadModel.{ RateSpreadError, RateSpreadResponse }
import hmda.model.apor.{ FixedRate, RateType, VariableRate }
import hmda.persistence.messages.commands.apor.APORCommands.CalculateRateSpread
import spray.json.{ DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat, SerializationException }

object RateSpreadProtocol extends DefaultJsonProtocol {

  implicit object RateTypeFormat extends RootJsonFormat[RateType] {
    override def read(json: JsValue): RateType = json match {
      case JsString("FixedRate") => FixedRate
      case JsString("VariableRate") => VariableRate
      case _ => throw new DeserializationException("Rate Type expected")
    }

    override def write(rateType: RateType): JsValue = rateType match {
      case FixedRate => JsString("FixedRate")
      case VariableRate => JsString("VariableRate")
      case msg => throw new SerializationException(s"Cannot serialize Rate Type: $msg")
    }
  }

  implicit object LocalDateFormat extends RootJsonFormat[LocalDate] {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    override def read(json: JsValue): LocalDate = json match {
      case JsString(s) =>
        LocalDate.parse(s, formatter)
      case msg => throw new DeserializationException(s"Cannot deserialize $msg")
    }

    override def write(localDate: LocalDate): JsValue = {
      JsString(localDate.format(formatter))
    }
  }

  implicit val calculateRateSpreadFormat = jsonFormat6(CalculateRateSpread.apply)
  implicit val rateSpreadResponseFormat = jsonFormat1(RateSpreadResponse.apply)
  implicit val rateSpreadErrorFormat = jsonFormat1(RateSpreadError.apply)
}
