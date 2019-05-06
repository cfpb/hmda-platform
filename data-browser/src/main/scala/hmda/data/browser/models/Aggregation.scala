package hmda.data.browser.models

import io.circe.Encoder

case class Aggregation(count: Long,
                       sum: Double,
                       race: Race,
                       actionTaken: ActionTaken)

object Aggregation {
  // Scala => JSON
  implicit val encoder: Encoder[Aggregation] =
    Encoder.forProduct4("count", "sum", "race", "action_taken")(
      agg => (agg.count, agg.sum, agg.race.entryName, agg.actionTaken.value)
    )
}
