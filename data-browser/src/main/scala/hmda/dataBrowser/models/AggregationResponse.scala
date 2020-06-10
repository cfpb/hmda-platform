package hmda.dataBrowser.models

import io.circe.{ Decoder, DecodingFailure, Encoder, HCursor, Json }
import cats.instances.list._
import cats.instances.either._
import cats.syntax.traverse._

case class Parameters(inputs: Seq[FieldInfo])

case class AggregationResponse(parameters: Parameters, aggregations: Seq[Aggregation], servedFrom: ServedFrom)

object Parameters {
  def fromBrowserFields(browserFields: List[QueryField]): Parameters =
    Parameters(browserFields.map(field => FieldInfo(field.name, field.values.mkString(","))))

  implicit val encoder: Encoder[Parameters] = (param: Parameters) => {
    val kvs = param.inputs.map {
      case FieldInfo(name, value) => name -> Json.fromString(value)
    }
    Json.obj(kvs: _*)
  }

  implicit val decoder: Decoder[Parameters] = { cursor: HCursor =>
    cursor.keys match {
      case None =>
        Left(DecodingFailure("Could not obtain keys for the Parameters JSON object", Nil))
      case Some(keys) =>
        val fieldKeys = keys.toList
        val fieldInfos: Either[DecodingFailure, List[FieldInfo]] = fieldKeys
          .map(key =>
            cursor
              .downField(key)
              .as[String]
              .map(value => FieldInfo(key, value))
          )
          .sequence

        fieldInfos.map(Parameters(_))
    }
  }
}

object AggregationResponse {
  private object constants {
    val Parameters   = "parameters"
    val Aggregations = "aggregations"
    val ServedFrom   = "servedFrom"
  }

  implicit val encoder: Encoder[AggregationResponse] =
    Encoder.forProduct3(constants.Parameters, constants.Aggregations, constants.ServedFrom)(aggR =>
      (aggR.parameters, aggR.aggregations, aggR.servedFrom)
    )

  implicit val decoder: Decoder[AggregationResponse] = (c: HCursor) =>
    for {
      p <- c.downField(constants.Parameters).as[Parameters]
      a <- c.downField(constants.Aggregations).as[Seq[Aggregation]]
    } yield AggregationResponse(p, a, ServedFrom.Database)
}
