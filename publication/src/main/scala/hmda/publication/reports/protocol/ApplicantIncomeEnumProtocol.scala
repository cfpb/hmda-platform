package hmda.publication.reports.protocol

import hmda.model.publication.reports.ApplicantIncomeEnum
import hmda.model.publication.reports.ApplicantIncomeEnum._
import spray.json.{ DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat }

trait ApplicantIncomeEnumProtocol extends DefaultJsonProtocol {

  implicit object ApplicantIncomeEnumFormat extends RootJsonFormat[ApplicantIncomeEnum] {

    override def write(obj: ApplicantIncomeEnum): JsValue = JsString(obj.description)

    override def read(json: JsValue): ApplicantIncomeEnum = json match {
      case JsString(description) => description match {
        case LessThan50PercentOfMSAMedian.description => LessThan50PercentOfMSAMedian
        case Between50And79PercentOfMSAMedian.description => Between50And79PercentOfMSAMedian
        case Between80And99PercentOfMSAMedian.description => Between80And99PercentOfMSAMedian
        case Between100And119PercentOfMSAMedian.description => Between100And119PercentOfMSAMedian
        case GreaterThan120PercentOfMSAMedian.description => GreaterThan120PercentOfMSAMedian
        case _ => throw DeserializationException(s"Unable to translate JSON string into valid Action Type value: $description")
      }

      case _ => throw DeserializationException("Unable to deserialize")
    }

  }
}
