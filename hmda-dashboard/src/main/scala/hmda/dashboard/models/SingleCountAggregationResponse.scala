package hmda.dashboard.models

import io.circe.{Decoder, DecodingFailure, Encoder, HCursor, Json}
import cats.instances.list._
import cats.instances.either._
import cats.syntax.traverse._

case class Parameters(inputs: Seq[FieldInfo])

case class SingleCountAggregationResponse(aggregations: Vector[TotalFilers])

object Parameters {

  def fromBrowserFields(browserFields: List[QueryField]): Parameters =
    Parameters(browserFields.map(field =>
      FieldInfo(field.name, field.values.mkString(","))))

  implicit val encoder: Encoder[Parameters] = (param: Parameters) => {
    val kvs = param.inputs.map {
      case FieldInfo(name, value) => name -> Json.fromString(value)
    }
    Json.obj(kvs: _*)
  }

  implicit val decoder: Decoder[Parameters] = { cursor: HCursor =>
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

        fieldInfos.map(Parameters(_))
    }
  }
}

object SingleCountAggregationResponse {
  private object constants {
    val Results = "results"
  }

  implicit val encoder: Encoder[SingleCountAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[SingleCountAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Vector[TotalFilers]]
    } yield SingleCountAggregationResponse(a)
}
