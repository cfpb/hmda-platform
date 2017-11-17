package hmda.publication.reports.protocol

import hmda.model.publication.reports.Disposition
import spray.json.DefaultJsonProtocol

trait DispositionProtocol extends DefaultJsonProtocol {
  implicit val dispositionFormat = jsonFormat3(Disposition.apply)
}
