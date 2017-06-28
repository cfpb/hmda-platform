package hmda.api.protocol.processing

import hmda.api.model.PaginationLinks
import spray.json.DefaultJsonProtocol

trait PaginationProtocol extends DefaultJsonProtocol {

  implicit val paginationLinksFormat = jsonFormat6(PaginationLinks.apply)

}
