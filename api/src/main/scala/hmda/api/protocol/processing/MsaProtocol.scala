package hmda.api.protocol.processing

import hmda.api.model.{ IrsResponse, PaginatedResponse }
import hmda.query.model.filing.{ MsaSummary, MsaWithName }
import spray.json._

trait MsaProtocol extends DefaultJsonProtocol with ParserResultsProtocol {
  implicit val msaWithNameProtocol = jsonFormat14(MsaWithName.apply)
  implicit val msaSummaryProtocol = jsonFormat12(MsaSummary.apply)

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
          val msaCollection = msas.map(_.convertTo[MsaWithName])
          val path = PaginatedResponse.staticPath(links("href").convertTo[String])
          val currentPage = PaginatedResponse.currentPage(links("self").convertTo[String])
          val summary = JsObject(summ).convertTo[MsaSummary]
          val total = tot.intValue
          IrsResponse(msaCollection.toList, summary, path, currentPage, total)

        case _ => throw DeserializationException("IRS Summary expected")
      }
    }
  }
}
