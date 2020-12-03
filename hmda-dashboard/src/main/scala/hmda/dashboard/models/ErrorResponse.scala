package hmda.dashboard.models

import io.circe.Encoder
import io.circe.generic.semiauto._

// $COVERAGE-OFF$
sealed trait ErrorResponse {
  def errorType: String
  def message: String
}

object ProvideDatetime {
  implicit val encoder: Encoder[ProvideDatetime] = deriveEncoder[ProvideDatetime]
}
final case class ProvideDatetime(errorType: String = "Bad Format", message: String = "Must provide date time in ##/##/#### ##:##:## format") extends ErrorResponse

// $COVERAGE-ON$