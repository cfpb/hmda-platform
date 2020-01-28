package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class FilersCountClosedEndOriginationsByAgencyGraterThanEqualAggregationResponse(aggregations: Seq[FilersCountClosedEndOriginationsByAgencyGraterOrEqual])

object FilersCountClosedEndOriginationsByAgencyGraterThanEqualAggregationResponse {
  private object constants {
    val Results = "results"
  }

  implicit val encoder: Encoder[FilersCountClosedEndOriginationsByAgencyGraterThanEqualAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[FilersCountClosedEndOriginationsByAgencyGraterThanEqualAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[FilersCountClosedEndOriginationsByAgencyGraterOrEqual]]
    } yield FilersCountClosedEndOriginationsByAgencyGraterThanEqualAggregationResponse(a)
}
