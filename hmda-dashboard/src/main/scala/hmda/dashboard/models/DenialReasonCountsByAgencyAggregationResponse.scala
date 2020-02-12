package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class DenialReasonCountsByAgencyAggregationResponse(aggregations: Seq[DenialReasonCountsByAgency])

object DenialReasonCountsByAgencyAggregationResponse {
  private object constants {
    val Results = "results"
  }

  implicit val encoder: Encoder[DenialReasonCountsByAgencyAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[DenialReasonCountsByAgencyAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[DenialReasonCountsByAgency]]
    } yield DenialReasonCountsByAgencyAggregationResponse(a)
}
