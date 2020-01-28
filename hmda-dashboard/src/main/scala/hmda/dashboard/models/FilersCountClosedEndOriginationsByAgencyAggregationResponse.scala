package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class FilersCountClosedEndOriginationsByAgencyAggregationResponse(aggregations: Seq[FilersCountClosedEndOriginationsByAgency])

object FilersCountClosedEndOriginationsByAgencyAggregationResponse {
  private object constants {
    val Results = "results"
  }

  implicit val encoder: Encoder[FilersCountClosedEndOriginationsByAgencyAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[FilersCountClosedEndOriginationsByAgencyAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[FilersCountClosedEndOriginationsByAgency]]
    } yield FilersCountClosedEndOriginationsByAgencyAggregationResponse(a)
}
