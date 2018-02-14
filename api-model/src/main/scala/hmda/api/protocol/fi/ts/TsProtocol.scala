package hmda.api.protocol.fi.ts

import hmda.model.fi.ts._
import spray.json.DefaultJsonProtocol

trait TsProtocol extends DefaultJsonProtocol {
  implicit val contactFormat = jsonFormat4(Contact.apply)
  implicit val parentFormat = jsonFormat5(Parent.apply)
  implicit val respondentFormat = jsonFormat6(Respondent.apply)
  implicit val tsFormat = jsonFormat9(TransmittalSheet.apply)
}