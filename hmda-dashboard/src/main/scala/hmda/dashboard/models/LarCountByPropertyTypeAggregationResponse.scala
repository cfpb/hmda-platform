package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class LarCountByPropertyTypeAggregationResponse(aggregations: Seq[LarCountByPropertyType])

object LarCountByPropertyTypeAggregationResponse {
  private object constants {
    val Results = "results"
  }

  implicit val encoder: Encoder[LarCountByPropertyTypeAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[LarCountByPropertyTypeAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[LarCountByPropertyType]]
    } yield LarCountByPropertyTypeAggregationResponse(a)
}
