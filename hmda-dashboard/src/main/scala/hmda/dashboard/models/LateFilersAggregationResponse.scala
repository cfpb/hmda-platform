package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class LateFilersAggregationResponse(aggregations: Seq[LateFilers])

object LateFilersAggregationResponse {
  private object constants {
    val Results = "estimated results"
  }

  implicit val encoder: Encoder[LateFilersAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[LateFilersAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[LateFilers]]
    } yield LateFilersAggregationResponse(a)
}