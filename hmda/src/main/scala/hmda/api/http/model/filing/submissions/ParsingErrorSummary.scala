package hmda.api.http.model.filing.submissions

import hmda.messages.submission.SubmissionProcessingEvents.HmdaRowParsedError
import hmda.model.filing.submission.{ParsedWithErrors, SubmissionStatus}
import io.circe._
import io.circe.syntax._

case class ParsingErrorSummary(transmittalSheetErrors: List[String] = Nil,
                               larErrors: List[HmdaRowParsedError] = Nil,
                               path: String = "",
                               currentPage: Int = 0,
                               total: Int = 0,
                               status: SubmissionStatus = ParsedWithErrors)
  extends PaginatedResponse {
  def isEmpty: Boolean =
    this.transmittalSheetErrors == Nil && this.larErrors == Nil && path == "" && currentPage == 0 && total == 0 && status == ParsedWithErrors
}

object ParsingErrorSummary {
  implicit val parsingErrorSummaryEncoder: Encoder[ParsingErrorSummary] =
    (a: ParsingErrorSummary) =>
      Json.obj(
        ("transmittalSheetErrors", a.transmittalSheetErrors.asJson),
        ("larErrors", a.larErrors.asJson),
        ("count", Json.fromInt(a.count)),
        ("total", Json.fromInt(a.total)),
        ("status", a.status.asJson),
        ("_links", a.links.asJson)
      )

  implicit val parsingErrorSummaryDecoder: Decoder[ParsingErrorSummary] =
    (c: HCursor) =>
      for {
        tsErrors <- c.downField("transmittalSheetErrors").as[List[String]]
        larErrors <- c.downField("larErrors").as[List[HmdaRowParsedError]]
        total <- c.downField("total").as[Int]
        status <- c.downField("status").as[SubmissionStatus]
        links <- c.downField("_links").as[PaginationLinks]
      } yield {
        val path = PaginatedResponse.staticPath(links.href)
        val currentPage = PaginatedResponse.currentPage(links.self)
        ParsingErrorSummary(tsErrors,
          larErrors,
          path,
          currentPage,
          total,
          status)
      }
}