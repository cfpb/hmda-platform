package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class FilerAttemptsAggregationResponse(aggregations: Seq[FilerAttempts])

object FilerAttemptsAggregationResponse {
  private object constants {
    val Results = "results"
  }

  implicit val encoder: Encoder[FilerAttemptsAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[FilerAttemptsAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[FilerAttempts]]
    } yield FilerAttemptsAggregationResponse(a)
}
