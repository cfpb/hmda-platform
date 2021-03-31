package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class ListQuarterlyFilersAggregationResponse(aggregations: Seq[ListQuarterlyFilers])

object ListQuarterlyFilersAggregationResponse {
  private object constants {
    val Results = "estimated results"
  }

  implicit val encoder: Encoder[ListQuarterlyFilersAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[ListQuarterlyFilersAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[ListQuarterlyFilers]]
    } yield ListQuarterlyFilersAggregationResponse(a)
}
