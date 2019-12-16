package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class TopFilersAggregationResponse(aggregations: Seq[TopFilers])

object TopFilersAggregationResponse {
  private object constants {
    val Results = "results"
  }

  implicit val encoder: Encoder[TopFilersAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[TopFilersAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[TopFilers]]
    } yield TopFilersAggregationResponse(a)
}
