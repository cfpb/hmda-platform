package hmda.api.protocol.processing

import hmda.api.model.{ PaginationLinks, ParsingErrorSummary, PaginatedResponse }
import hmda.parser.fi.lar.LarParsingError
import spray.json._

trait ParserResultsProtocol extends DefaultJsonProtocol {

  implicit val larParsingErrorFormat = jsonFormat2(LarParsingError.apply)
  implicit val paginationLinksFormat = jsonFormat6(PaginationLinks.apply)

  implicit object ParsingSummaryJsonFormat extends RootJsonFormat[ParsingErrorSummary] {
    override def write(summary: ParsingErrorSummary): JsValue = {
      JsObject(
        "transmittalSheetErrors" -> summary.transmittalSheetErrors.toJson,
        "larErrors" -> summary.larErrors.toJson,
        "count" -> JsNumber(summary.count),
        "total" -> JsNumber(summary.total),
        "_links" -> summary.links.toJson
      )
    }

    override def read(json: JsValue): ParsingErrorSummary = {
      json.asJsObject.getFields("transmittalSheetErrors", "larErrors", "count", "total", "_links") match {
        case Seq(JsArray(ts), JsArray(lar), JsNumber(_), JsNumber(tot), JsObject(links)) =>
          val tsErrs: Seq[String] = ts.map(_.convertTo[String])
          val larErrs: Seq[LarParsingError] = lar.map(_.convertTo[LarParsingError])
          val path: String = PaginatedResponse.staticPath(links("href").convertTo[String])
          val currentPage: Int = PaginatedResponse.currentPage(links("self").convertTo[String])
          val total: Int = tot.intValue
          ParsingErrorSummary(tsErrs, larErrs, path, currentPage, total)

        case _ => throw DeserializationException("Parsing Error Summary expected")
      }
    }

  }

}

