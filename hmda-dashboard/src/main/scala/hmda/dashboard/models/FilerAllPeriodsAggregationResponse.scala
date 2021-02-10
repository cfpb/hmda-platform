package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class FilerAllPeriodsAggregationResponse(aggregations: Seq[FilerAllPeriods])

object FilerAllPeriodsAggregationResponse {
  private object constants {
    val Results = "results"
  }

  implicit val encoder: Encoder[FilerAllPeriodsAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[FilerAllPeriodsAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[FilerAllPeriods]]
    } yield FilerAllPeriodsAggregationResponse(a)
}
