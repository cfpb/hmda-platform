package hmda.api.model.public

object RateSpreadModel {
  case class RateSpreadResponse(rateSpread: String)
  case class RateSpreadError(error: String)
}
