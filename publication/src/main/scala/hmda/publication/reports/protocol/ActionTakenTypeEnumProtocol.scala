package hmda.publication.reports.protocol

import hmda.model.publication.reports.ActionTakenTypeEnum
import hmda.model.publication.reports.ActionTakenTypeEnum._
import spray.json.{ DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat }

trait ActionTakenTypeEnumProtocol extends DefaultJsonProtocol {

  implicit object ActionTakenTypeEnumFormat extends RootJsonFormat[ActionTakenTypeEnum] {

    override def write(obj: ActionTakenTypeEnum): JsValue = JsString(obj.description)

    override def read(json: JsValue): ActionTakenTypeEnum = json match {
      case JsString(description) => description match {
        case ApplicationReceived.description => ApplicationReceived
        case LoansOriginated.description => LoansOriginated
        case ApprovedButNotAccepted.description => ApprovedButNotAccepted
        case ApplicationsDenied.description => ApplicationsDenied
        case ApplicationsWithdrawn.description => ApplicationsWithdrawn
        case ClosedForIncompleteness.description => ClosedForIncompleteness
        case LoanPurchased.description => LoanPurchased
        case PreapprovalDenied.description => PreapprovalDenied
        case PreapprovalApprovedButNotAccepted.description => PreapprovalApprovedButNotAccepted
        case _ => throw DeserializationException(s"Unable to translate JSON string into valid Action Type value: $description")
      }
      case _ => throw DeserializationException("Unable to deserialize")

    }

  }

}
