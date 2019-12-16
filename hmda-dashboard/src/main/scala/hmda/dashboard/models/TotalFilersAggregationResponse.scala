package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class TotalFilersAggregationResponse(aggregations: Seq[TotalFilers])

object TotalFilersAggregationResponse {
  private object constants {
    val Results = "results"
  }

  implicit val encoder: Encoder[TotalFilersAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[TotalFilersAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[TotalFilers]]
    } yield TotalFilersAggregationResponse(a)
}
