package hmda.dashboard.models

import io.circe.{Decoder, DecodingFailure, Encoder, HCursor, Json}
import cats.instances.either._
import cats.instances.list._
import cats.syntax.traverse._

case class FieldInfo(name: String, value: String)
case class SingleCountAggregation(count: Long)

object SingleCountAggregation {
  private object constants {
    val Count = "count"
  }

  implicit val encoder: Encoder[SingleCountAggregation] = (agg: SingleCountAggregation) => {
    val jsonKVs: List[(String, Json)] = List(
      constants.Count -> Json.fromLong(agg.count),
    )

    Json.obj(jsonKVs: _*)
  }

  implicit val decoder: Decoder[SingleCountAggregation] = { cursor: HCursor =>
    cursor.keys match {
      case None =>
        Left(
          DecodingFailure(
            "Could not obtain keys for the Aggregation JSON object",
            Nil))
      case Some(keys) =>
        val fieldKeys = keys.toList
          .filterNot(_ == constants.Count)
        val fieldInfos: Either[DecodingFailure, List[FieldInfo]] = fieldKeys
          .map(
            key =>
              cursor
                .downField(key)
                .as[String]
                .map(value => FieldInfo(key, value)))
          .sequence

        val countAndSum: Either[DecodingFailure, (Long)] = for {
          count <- cursor.downField(constants.Count).as[Long]
        } yield (count)

        for {
          countAndSum <- countAndSum
          (count) = countAndSum
        } yield SingleCountAggregation(count)
    }
  }
}
