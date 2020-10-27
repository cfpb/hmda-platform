package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class QuarterDetailsAggregationsResponse(aggregations: Seq[QuarterDetails])

object QuarterDetailsAggregationsResponse {
  private object constants {
    val Results = "results"
  }

  implicit val encoder: Encoder[QuarterDetailsAggregationsResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[QuarterDetailsAggregationsResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[QuarterDetails]]
    } yield QuarterDetailsAggregationsResponse(a)
}
