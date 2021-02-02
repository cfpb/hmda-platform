package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class FilersByLarAggregationResponse(aggregations: Seq[FilersByLar])

object FilersByLarAggregationResponse {
  private object constants {
    val Results = "results"
  }

  implicit val encoder: Encoder[FilersByLarAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[FilersByLarAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[FilersByLar]]
    } yield FilersByLarAggregationResponse(a)
}
