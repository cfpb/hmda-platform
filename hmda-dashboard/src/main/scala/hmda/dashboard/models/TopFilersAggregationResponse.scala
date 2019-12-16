package hmda.dashboard.models

import io.circe.{Decoder, DecodingFailure, Encoder, HCursor, Json}
import cats.instances.list._
import cats.instances.either._
import cats.syntax.traverse._

case class TopFilersParameters(inputs: List[FieldInfo])

case class TopFilersAggregationResponse(aggregations: Seq[TopFilers])

object TopFilersParameters {

  implicit val encoder: Encoder[TopFilersParameters] = (param: TopFilersParameters) => {
    val kvs = param.inputs.map {
      case FieldInfo(name, value) => name -> Json.fromString(value)
    }
    Json.obj(kvs: _*)
  }

  implicit val decoder: Decoder[TopFilersParameters] = { cursor: HCursor =>
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

        fieldInfos.map(TopFilersParameters(_))
    }
  }
}

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
