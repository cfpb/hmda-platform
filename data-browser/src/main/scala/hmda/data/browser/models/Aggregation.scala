package hmda.data.browser.models

import io.circe.{Decoder, Encoder, HCursor}

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
  implicit val encoder: Encoder[Aggregation] =
    Encoder.forProduct4(constants.Count,
                        constants.Sum,
                        constants.Field1,
                        constants.Field2)(
      agg => (agg.count, agg.sum, agg.field1.value, agg.field2.value)
    )

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
