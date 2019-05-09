package hmda.data.browser.models

import io.circe.{Decoder, Encoder, HCursor}

case class Parameters(msaMd: Option[Int],
                      state: Option[String],
                      races: Seq[String],
                      actionsTaken: Seq[Int])
case class AggregationResponse(parameters: Parameters,
                               aggregations: Seq[Aggregation])

object Parameters {
  private object constants {
    val MsaMd = "msamd"
    val State = "state"
    val Races = "races"
    val ActionsTaken = "actions_taken"
  }

  implicit val encoder: Encoder[Parameters] = {
    val c = constants
    Encoder.forProduct4(c.MsaMd, c.State, c.Races, c.ActionsTaken)(p =>
      (p.msaMd, p.state, p.races, p.actionsTaken))
  }

  implicit val decoder: Decoder[Parameters] = (c: HCursor) => {
    val cons = constants
    for {
      msaMd <- c.downField(cons.MsaMd).as[Option[Int]]
      state <- c.downField(cons.State).as[Option[String]]
      races <- c.downField(cons.Races).as[Seq[String]]
      actionsTaken <- c.downField(cons.ActionsTaken).as[Seq[Int]]
    } yield Parameters(msaMd, state, races, actionsTaken)
  }
}

object AggregationResponse {
  private object constants {
    val Parameters = "parameters"
    val Aggregations = "aggregations"
  }

  implicit val encoder: Encoder[AggregationResponse] =
    Encoder.forProduct2(constants.Parameters, constants.Aggregations)(aggR =>
      (aggR.parameters, aggR.aggregations))

  implicit val decoder: Decoder[AggregationResponse] = (c: HCursor) =>
    for {
      p <- c.downField(constants.Parameters).as[Parameters]
      a <- c.downField(constants.Aggregations).as[Seq[Aggregation]]
    } yield AggregationResponse(p, a)
}
