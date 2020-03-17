package hmda.api.http.model.filing.submissions

import hmda.model.filing.Filing
import hmda.model.filing.submission.Submission
import io.circe._
import io.circe.syntax._

object FilingDetailsSummary {
  implicit val encoder: Encoder[FilingDetailsSummary] = fds =>
    Json.obj(
      "filing" := fds.filing,
      "submissions" := fds.submissions,
      "count" := fds.count,
      "total" := fds.total,
      "_links" := fds.links
    )
}
case class FilingDetailsSummary(
                                 filing: Filing,
                                 submissions: List[Submission],
                                 total: Int,
                                 currentPage: Int,
                                 path: String
                               ) extends PaginatedResponse