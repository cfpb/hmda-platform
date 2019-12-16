package hmda.dashboard.models

import io.circe.{Decoder, DecodingFailure, Encoder, HCursor, Json}
import cats.instances.list._
import cats.instances.either._
import cats.syntax.traverse._

case class FilerParameters(inputs: List[FieldInfo])

case class TotalFilersAggregationResponse(aggregations: Seq[TotalFilers])

object FilerParameters {

  implicit val encoder: Encoder[FilerParameters] = (param: FilerParameters) => {
    val kvs = param.inputs.map {
      case FieldInfo(name, value) => name -> Json.fromString(value)
    }
    Json.obj(kvs: _*)
  }

  implicit val decoder: Decoder[FilerParameters] = { cursor: HCursor =>
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

        fieldInfos.map(FilerParameters(_))
    }
  }
}

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
