package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class FilersByAgencyAgggregationResponse(aggregations: Seq[FilersByAgency])

object FilersByAgencyAgggregationResponse {
  private object constants {
    val Results = "results"
  }

  implicit val encoder: Encoder[FilersByAgencyAgggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[FilersByAgencyAgggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[FilersByAgency]]
    } yield FilersByAgencyAgggregationResponse(a)
}
