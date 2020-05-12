package hmda.api.ws.model

// $COVERAGE-OFF$
trait WSResponseType {
  val messageType: String
}

case object ServerPing extends WSResponseType {
  override val messageType: String = "ServerPing"
}

case object SubmissionStatus extends WSResponseType {
  override val messageType: String = "SubmissionStatus"
}
// $COVERAGE-ON$