package hmda.api.protocol.processing

import hmda.api.model.{ IrsResponse, PaginatedResponse, PaginationLinks }
import hmda.query.model.filing.{ Msa, MsaSummary }
import spray.json._

trait MsaProtocol extends DefaultJsonProtocol with ParserResultsProtocol {
  implicit val msaProtocol = jsonFormat13(Msa.apply)
  implicit val msaSummaryProtocol = jsonFormat12(MsaSummary.apply)
  //implicit val paginationLinkFormat = jsonFormat6(PaginationLinks.apply)

  implicit object IrsResponseJsonFormat extends RootJsonFormat[IrsResponse] {
    override def write(irs: IrsResponse): JsValue = {
      JsObject(
        "msas" -> irs.msas.toJson,
        "summary" -> irs.summary.toJson,
        "count" -> JsNumber(irs.count),
        "total" -> JsNumber(irs.total),
        "_links" -> irs.links.toJson
      )
    }

    override def read(json: JsValue): IrsResponse = {
      json.asJsObject.getFields("msas", "summary", "total", "_links") match {
        case Seq(JsArray(msas), JsObject(summ), JsNumber(tot), JsObject(links)) =>
          val msaCollection: Seq[Msa] = msas.map(_.convertTo[Msa])
          val path: String = PaginatedResponse.staticPath(links("href").convertTo[String])
          val currentPage: Int = PaginatedResponse.currentPage(links("self").convertTo[String])
          val summary: MsaSummary = JsObject(summ).convertTo[MsaSummary]
          val total: Int = tot.intValue
          IrsResponse(msaCollection.toList, summary, path, currentPage, total)

        case _ => throw DeserializationException("IRS Summary expected")
      }
    }
  }
}
