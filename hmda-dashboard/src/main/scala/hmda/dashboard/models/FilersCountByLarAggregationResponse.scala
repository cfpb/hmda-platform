package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class FilersCountByLarAggregationResponse(aggregations: Seq[FilersCountByLar])

object FilersCountByLarAggregationResponse {
  private object constants {
    val Results = "results"
  }

  implicit val encoder: Encoder[FilersCountByLarAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[FilersCountByLarAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[FilersCountByLar]]
    } yield FilersCountByLarAggregationResponse(a)
}


