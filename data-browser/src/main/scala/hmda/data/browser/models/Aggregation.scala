package hmda.data.browser.models

import io.circe.{Decoder, Encoder, HCursor}

case class Aggregation(count: Long,
                       sum: Double,
                       race: Race,
                       actionTaken: ActionTaken)

object Aggregation {
  private object constants {
    val Count = "count"
    val Sum = "sum"
    val Race = "race"
    val ActionTaken = "action_taken"
  }

  // Scala => JSON
  implicit val encoder: Encoder[Aggregation] =
    Encoder.forProduct4(constants.Count,
                        constants.Sum,
                        constants.Race,
                        constants.ActionTaken)(
      agg => (agg.count, agg.sum, agg.race.entryName, agg.actionTaken.value)
    )

  implicit val decoder: Decoder[Aggregation] = (h: HCursor) =>
    for {
      count <- h.downField(constants.Count).as[Long]
      sum <- h.downField(constants.Sum).as[Double]
      race <- h
        .downField(constants.Race)
        .as[String]
        .map(Race.withNameInsensitive)
      actionTaken <- h
        .downField(constants.ActionTaken)
        .as[Int]
        .map(ActionTaken.withValue)
    } yield Aggregation(count, sum, race, actionTaken)
}
