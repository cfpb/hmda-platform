package hmda.dashboard.models

import io.circe.{Decoder, DecodingFailure, Encoder, HCursor, Json}
import cats.instances.list._
import cats.instances.either._
import cats.syntax.traverse._

case class FilerAttemptsParameters(inputs: List[FieldInfo])

case class FilerAttemptsAggregationResponse(aggregations: Seq[FilerAttempts])

object FilerAttemptsParameters {

  implicit val encoder: Encoder[FilerAttemptsParameters] = (param: FilerAttemptsParameters) => {
    val kvs = param.inputs.map {
      case FieldInfo(name, value) => name -> Json.fromString(value)
    }
    Json.obj(kvs: _*)
  }

  implicit val decoder: Decoder[FilerAttemptsParameters] = { cursor: HCursor =>
    cursor.keys match {
      case None =>
        Left(
          DecodingFailure(
            "Could not obtain keys for the Parameters JSON object",
            Nil))
      case Some(keys) =>
        val fieldKeys = keys.toList
        val fieldInfos: Either[DecodingFailure, List[FieldInfo]] = fieldKeys
          .map(
            key =>
              cursor
                .downField(key)
                .as[String]
                .map(value => FieldInfo(key, value)))
          .sequence

        fieldInfos.map(FilerAttemptsParameters(_))
    }
  }
}

object FilerAttemptsAggregationResponse {
  private object constants {
    val Results = "results"
  }

  implicit val encoder: Encoder[FilerAttemptsAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[FilerAttemptsAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[FilerAttempts]]
    } yield FilerAttemptsAggregationResponse(a)
}
