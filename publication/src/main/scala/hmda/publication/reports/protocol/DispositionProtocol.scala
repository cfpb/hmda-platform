package hmda.publication.reports.protocol

import hmda.model.publication.reports.Disposition
import spray.json.DefaultJsonProtocol

trait DispositionProtocol extends DefaultJsonProtocol with ActionTakenTypeEnumProtocol {
  implicit val dispositionFormat = jsonFormat3(Disposition.apply)
}
