package hmda.apiModel.protocol.processing

import hmda.apiModel.model.PaginationLinks
import spray.json.DefaultJsonProtocol

trait PaginationProtocol extends DefaultJsonProtocol {

  implicit val paginationLinksFormat = jsonFormat6(PaginationLinks.apply)

}
