package hmda.publication.reports.protocol

import hmda.model.publication.reports.RaceEnum
import hmda.model.publication.reports.RaceEnum._
import spray.json.{ DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat }

trait RaceEnumProtocol extends DefaultJsonProtocol {

  implicit object RaceEnumFormat extends RootJsonFormat[RaceEnum] {

    override def write(obj: RaceEnum): JsValue = JsString(obj.description)

    override def read(json: JsValue): RaceEnum = json match {
      case JsString(description) => description match {
        case AmericanIndianOrAlaskaNative.description => AmericanIndianOrAlaskaNative
        case Asian.description => Asian
        case BlackOrAfricanAmerican.description => BlackOrAfricanAmerican
        case HawaiianOrPacific.description => HawaiianOrPacific
        case White.description => White
        case NotProvided.description => NotProvided
        case TwoOrMoreMinority.description => TwoOrMoreMinority
        case Joint.description => Joint
        case _ => throw DeserializationException(s"Unable to translate JSON string into valid Action Type value: $description")
      }

      case _ => throw DeserializationException("Unable to deserialize")
    }

  }
}
