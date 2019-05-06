package hmda.data.browser.models

 case class Parameters(msaMd: Option[Int],
                      state: Option[String],
                      races: Seq[String],
                      actionsTaken: Seq[Int])
case class AggregationResponse(parameters: Parameters,
                               aggregations: Seq[Aggregation])