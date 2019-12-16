package hmda.dashboard.models

import io.circe.{Decoder, DecodingFailure, Encoder, HCursor, Json}
import cats.instances.list._
import cats.instances.either._
import cats.syntax.traverse._

case class SingleAttempetParameters(inputs: List[FieldInfo])

case class SingleAttemptsAggregationResponse(aggregations: Seq[SingleAttempts])

object SingleAttempetParameter {

  implicit val encoder: Encoder[SingleAttempetParameters] = (param: SingleAttempetParameters) => {
    val kvs = param.inputs.map {
      case FieldInfo(name, value) => name -> Json.fromString(value)
    }
    Json.obj(kvs: _*)
  }

  implicit val decoder: Decoder[SingleAttempetParameters] = { cursor: HCursor =>
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

        fieldInfos.map(SingleAttempetParameters(_))
    }
  }
}

object SingleAttemptsAggregationResponse {
  private object constants {
    val Results = "results"
  }

  implicit val encoder: Encoder[SingleAttemptsAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[SingleAttemptsAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[SingleAttempts]]
    } yield SingleAttemptsAggregationResponse(a)
}
