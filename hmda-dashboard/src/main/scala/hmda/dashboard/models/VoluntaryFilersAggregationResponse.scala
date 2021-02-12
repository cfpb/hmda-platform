package hmda.dashboard.models

import io.circe.{Decoder, Encoder, HCursor}

case class VoluntaryFilersAggregationResponse(aggregations: Seq[VoluntaryFilers])

object VoluntaryFilersAggregationResponse {

  private object constants {
    val Results = "estimated results"
  }

  implicit val encoder: Encoder[VoluntaryFilersAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[VoluntaryFilersAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[VoluntaryFilers]]
    } yield VoluntaryFilersAggregationResponse(a)
}