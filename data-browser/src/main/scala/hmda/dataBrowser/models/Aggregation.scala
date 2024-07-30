package hmda.dataBrowser.models

import io.circe.{Decoder, DecodingFailure, Encoder, HCursor, Json}
import cats.instances.either._
import cats.instances.list._
import cats.syntax.traverse._

case class FieldInfo(name: String, value: String)
case class Aggregation(count: Long, sum: Double, fields: List[FieldInfo])

object Aggregation {

  private object constants {
    val Count = "count"
    val Sum = "sum"
  }

  implicit def sumAggregation(aggregation: Aggregation, secondAggregation: Aggregation): Aggregation = {
    Aggregation(aggregation.count + secondAggregation.count, aggregation.sum + secondAggregation.sum, aggregation.fields)
  }

  // Scala => JSON
  implicit val encoder: Encoder[Aggregation] = (agg: Aggregation) => {
    val jsonKVs: List[(String, Json)] = List(
      constants.Count -> Json.fromLong(agg.count),
      constants.Sum -> Json.fromDoubleOrNull(agg.sum)
    ) ++ agg.fields.map(field => field.name -> Json.fromString(field.value))

    Json.obj(jsonKVs: _*)
  }

  implicit val decoder: Decoder[Aggregation] = { cursor: HCursor =>
    cursor.keys match {
      case None =>
        Left(
          DecodingFailure(
            "Could not obtain keys for the Aggregation JSON object",
            Nil))
      case Some(keys) =>
        val fieldKeys = keys.toList
          .filterNot(_ == constants.Count)
          .filterNot(_ == constants.Sum)
        val fieldInfos: Either[DecodingFailure, List[FieldInfo]] = fieldKeys
          .map(
            key =>
              cursor
                .downField(key)
                .as[String]
                .map(value => FieldInfo(key, value)))
          .sequence

        val countAndSum: Either[DecodingFailure, (Long, Double)] = for {
          count <- cursor.downField(constants.Count).as[Long]
          sum <- cursor.downField(constants.Sum).as[Double]
        } yield (count, sum)

        for {
          countAndSum <- countAndSum
          (count, sum) = countAndSum
          fields <- fieldInfos
        } yield Aggregation(count, sum, fields)
    }
  }
}
