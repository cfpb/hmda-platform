package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class FilersCountOpenEndOriginationsByAgencyAggregationResponse(aggregations: Seq[FilersCountOpenEndOriginationsByAgency])

object FilersCountOpenEndOriginationsByAgencyAggregationResponse {
  private object constants {
    val Results = "results"
  }

  implicit val encoder: Encoder[FilersCountOpenEndOriginationsByAgencyAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[FilersCountOpenEndOriginationsByAgencyAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[FilersCountOpenEndOriginationsByAgency]]
    } yield FilersCountOpenEndOriginationsByAgencyAggregationResponse(a)
}
