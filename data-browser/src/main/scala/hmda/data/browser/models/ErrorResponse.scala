package hmda.data.browser.models

sealed trait ErrorResponse {
  def errorType: String
}
final case class InvalidRaces(invalidRaces: Seq[String],
                              errorType: String = "invalid-races")
    extends ErrorResponse
final case class InvalidActions(invalidActions: Seq[Int],
                                errorType: String = "invalid-actions")
    extends ErrorResponse