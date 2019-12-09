package hmda.api.http.model.filing.submissions

import hmda.model.filing.submission.{ParsedWithErrors, SubmissionStatus}
import io.circe._
import io.circe.syntax._

case class FieldParserErrorSummary(fieldName: String,
                                   inputValue: String,
                                   validValues: String)

case class HmdaRowParsedErrorSummary(
    rowNumber: Int,
    estimatedULI: String,
    errorMessages: List[FieldParserErrorSummary]){
      def toCsv: String = {
        errorMessages.map(error => 
          s"$rowNumber|$estimatedULI|${error.fieldName}|${error.inputValue}|${error.validValues}\n"
        ).reduce(_ + _)
      }
    }

case class ParsingErrorSummary(
    transmittalSheetErrors: Seq[HmdaRowParsedErrorSummary] = Nil,
    larErrors: Seq[HmdaRowParsedErrorSummary] = Nil,
    path: String = "",
    currentPage: Int = 0,
    total: Int = 0,
    status: SubmissionStatus = ParsedWithErrors)
    extends PaginatedResponse {
  def isEmpty: Boolean =
    this.transmittalSheetErrors == Nil && this.larErrors == Nil && path == "" && currentPage == 0 && total == 0 && status == ParsedWithErrors
}

object ParsingErrorSummary {
  implicit val FieldParserErrorSummaryEncoder: Encoder[FieldParserErrorSummary] =
    (a: FieldParserErrorSummary) =>
      Json.obj(
        ("fieldName", Json.fromString(a.fieldName)),
        ("inputValue", Json.fromString(a.inputValue)),
        ("validValues", Json.fromString(a.validValues))
      )

  implicit val FieldParserErrorSummaryDecoder: Decoder[FieldParserErrorSummary] =
    (c: HCursor) =>
      for {
        fieldName <- c.downField("fieldName").as[String]
        inputValue <- c.downField("inputValue").as[String]
        validValues <- c.downField("validValues").as[String]
      } yield {
        FieldParserErrorSummary(fieldName,
          inputValue,
          validValues)
      }
  
  
  implicit val hmdaRowParsedErrorSummaryEncoder: Encoder[HmdaRowParsedErrorSummary] =
    (a: HmdaRowParsedErrorSummary) =>
      Json.obj(
        ("rowNumber", Json.fromInt(a.rowNumber)),
        ("estimatedULI", a.estimatedULI.asJson),
        ("errorMessages", a.errorMessages.asJson)
      )

  implicit val hmdaRowParsedErrorSummaryDecoder: Decoder[HmdaRowParsedErrorSummary] =
    (c: HCursor) =>
      for {
        rowNumber <- c.downField("rowNumber").as[Int]
        estimatedULI <- c.downField("estimatedULI").as[String]
        errorMessages <- c.downField("errorMessages").as[List[FieldParserErrorSummary]]
      } yield {
        HmdaRowParsedErrorSummary(rowNumber,
          estimatedULI,
          errorMessages)
      }
  
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
        tsErrors <- c.downField("transmittalSheetErrors").as[Seq[HmdaRowParsedErrorSummary]]
        larErrors <- c.downField("larErrors").as[Seq[HmdaRowParsedErrorSummary]]
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