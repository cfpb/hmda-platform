package hmda.data.browser.models

import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.syntax._

case class Aggregation(count: Long,
                       sum: Double,
                       field1: BrowserField,
                       field2: BrowserField)

object Aggregation {
  private object constants {
    val Count = "count"
    val Sum = "sum"
    val Field1 = "field 1"
    val Field2 = "field 2"
  }

  // Scala => JSON
  implicit val encoder = new Encoder[Aggregation] {
    final def apply(agg: Aggregation): Json =
      if (agg.field1.name == "empty") {
        Json.obj(
          ("count", agg.count.asJson),
          ("sum", agg.sum.asJson)
        )
      } else if (agg.field2.name == "empty") {
        Json.obj(
          ("count", agg.count.asJson),
          ("sum", agg.sum.asJson),
          (agg.field1.name, (agg.field1.value.toList).asJson)
        )
      } else {
        Json.obj(
          ("count", agg.count.asJson),
          ("sum", agg.sum.asJson),
          (agg.field1.name, (agg.field1.value.toList).asJson),
          (agg.field2.name, (agg.field2.value.toList).asJson)
        )
      }
  }

  implicit val decoder: Decoder[Aggregation] = (h: HCursor) =>
    for {
      count <- h.downField(constants.Count).as[Long]
      sum <- h.downField(constants.Sum).as[Double]
      field1 <- h
        .downField(constants.Field1)
        .as[String]
        .map(x => BrowserField("", Seq(x), "", ""))
      field2 <- h
        .downField(constants.Field2)
        .as[String]
        .map(x => BrowserField("", Seq(x), "", ""))
    } yield Aggregation(count, sum, field1, field2)
}
