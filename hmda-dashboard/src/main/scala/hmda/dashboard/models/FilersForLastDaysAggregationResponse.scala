package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class FilersForLastDaysAggregationResponse(aggregations: Seq[FilersForLastDays])

object FilersForLastDaysAggregationResponse {
  private object constants {
    val Results = "results"
  }

  implicit val encoder: Encoder[FilersForLastDaysAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[FilersForLastDaysAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[FilersForLastDays]]
    } yield FilersForLastDaysAggregationResponse(a)
}