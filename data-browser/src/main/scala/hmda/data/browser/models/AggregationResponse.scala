package hmda.data.browser.models

import io.circe.{Decoder, Encoder, HCursor}

case class Parameters(msaMd: Option[Int],
                      state: Option[String],
                      field1: BrowserField,
                      field2: BrowserField)
case class AggregationResponse(parameters: Parameters,
                               aggregations: Seq[Aggregation])

object Parameters {
  private object constants {
    val MsaMd = "msamd"
    val State = "state"
    val Field1 = "field 1"
    val Field2 = "field 2"
  }

  implicit val encoder: Encoder[Parameters] = {
    val c = constants
    Encoder.forProduct4(c.MsaMd, c.State, c.Field1, c.Field2)(p =>
      (p.msaMd, p.state, p.field1.value, p.field2.value))
  }

  implicit val decoder: Decoder[Parameters] = (c: HCursor) => {
    val cons = constants
    for {
      msaMd <- c.downField(cons.MsaMd).as[Option[Int]]
      state <- c.downField(cons.State).as[Option[String]]
      field1 <- c.downField(cons.Field1).as[Seq[String]]
      field2 <- c.downField(cons.Field2).as[Seq[String]]
    } yield
      Parameters(msaMd,
                 state,
                 BrowserField("", field1, "", ""),
                 BrowserField("", field2, "", ""))
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
