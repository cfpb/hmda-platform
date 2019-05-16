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
    val Field1 = field1.name
    val Field2 = field2.name
  }

  // Scala => JSON
  implicit val encoder: Encoder[Aggregation] =
    Encoder.forProduct4(constants.Count,
                        constants.Sum,
                        constants.Field1,
                        constants.Field2)(
      agg => (agg.count, agg.sum, agg.Field1.entryName, agg.Field2.value)
    )

  implicit val decoder: Decoder[Aggregation] = (h: HCursor) =>
    for {
      count <- h.downField(constants.Count).as[Long]
      sum <- h.downField(constants.Sum).as[Double]
      field1 <- h
        .downField(constants.Field1)
        .as[String]
        .map(Field1.withNameInsensitive)
      field2 <- h
        .downField(constants.Field2)
        .as[String]
        .map(Field2.withValue)
    } yield Aggregation(count, sum, Field1, Field2)
}
