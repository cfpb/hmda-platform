package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class MultipleAttemptsAggregationResponse(aggregations: Seq[MultipleAttempts])

object MultipleAttemptsAggregationResponse {
  private object constants {
    val Results = "results"
  }

  implicit val encoder: Encoder[MultipleAttemptsAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[MultipleAttemptsAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[MultipleAttempts]]
    } yield MultipleAttemptsAggregationResponse(a)
}
