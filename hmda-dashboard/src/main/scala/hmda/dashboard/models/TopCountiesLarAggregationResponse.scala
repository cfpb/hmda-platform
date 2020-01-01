package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class TopCountiesLarAggregationResponse(aggregations: Seq[TopCountiesLar])

object TopCountiesLarAggregationResponse {
  private object constants {
    val Results = "results"
  }

  implicit val encoder: Encoder[TopCountiesLarAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[TopCountiesLarAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[TopCountiesLar]]
    } yield TopCountiesLarAggregationResponse(a)
}
