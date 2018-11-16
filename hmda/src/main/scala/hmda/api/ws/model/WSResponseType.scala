package hmda.api.ws.model

trait WSResponseType {
  val messageType: String
}

case object ServerPing extends WSResponseType {
  override val messageType: String = "ServerPing"
}
