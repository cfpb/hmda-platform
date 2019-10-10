package hmda.api.http.model.filing.submissions

import hmda.model.edits.EditDetailsRow
import io.circe._
import io.circe.syntax._

case class EditDetailsSummary(editName: String = "",
                              rows: Seq[EditDetailsRow] = Nil,
                              path: String = "",
                              currentPage: Int = 0,
                              total: Int = 0)
  extends PaginatedResponse {
  def isEmpty: Boolean =
    this.rows == Nil && this.editName == "" && this.path == "" && this.currentPage == 0 && this.total == 0
}

object EditDetailsSummary {
  implicit val editDetailsSummaryEncoder: Encoder[EditDetailsSummary] =
    (a: EditDetailsSummary) =>
      Json.obj(
        ("edit", Json.fromString(a.editName)),
        ("rows", a.rows.asJson),
        ("count", Json.fromInt(a.count)),
        ("total", Json.fromInt(a.total)),
        ("_links", a.links.asJson)
      )

  implicit val editDetailsSummaryDecoder: Decoder[EditDetailsSummary] =
    (c: HCursor) =>
      for {
        edit <- c.downField("edit").as[String]
        rows <- c.downField("rows").as[Seq[EditDetailsRow]]
        total <- c.downField("total").as[Int]
        links <- c.downField("_links").as[PaginationLinks]
      } yield {
        val path = PaginatedResponse.staticPath(links.href)
        val currentPage = PaginatedResponse.currentPage(links.self)
        EditDetailsSummary(
          edit,
          rows,
          path,
          currentPage,
          total
        )
      }
}