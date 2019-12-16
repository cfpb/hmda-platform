package hmda.dashboard.models

import io.circe.{Decoder, DecodingFailure, Encoder, HCursor, Json}
import cats.instances.list._
import cats.instances.either._
import cats.syntax.traverse._

case class FilersForLastDaysParameters(inputs: List[FieldInfo])

case class FilersForLastDaysAggregationResponse(aggregations: Seq[FilersForLastDays])

object FilersForLastDaysParameters {

  implicit val encoder: Encoder[FilersForLastDaysParameters] = (param: FilersForLastDaysParameters) => {
    val kvs = param.inputs.map {
      case FieldInfo(name, value) => name -> Json.fromString(value)
    }
    Json.obj(kvs: _*)
  }

  implicit val decoder: Decoder[FilersForLastDaysParameters] = { cursor: HCursor =>
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

        fieldInfos.map(FilersForLastDaysParameters(_))
    }
  }
}

object FilersForLastDaysAggregationResponse {
  private object constants {
    val Results = "results"
  }

  implicit val encoder: Encoder[FilersForLastDaysAggregationResponse] =
    Encoder.forProduct1(constants.Results)(aggR =>
      aggR.aggregations)

  implicit val decoder: Decoder[FilersForLastDaysAggregationResponse] = (c: HCursor) =>
    for {
      a <- c.downField(constants.Results).as[Seq[FilersForLastDays]]
    } yield FilersForLastDaysAggregationResponse(a)
}