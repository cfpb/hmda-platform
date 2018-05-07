package hmda.model.messages

object CommonMessages {
  sealed trait Message
  case object StopActor extends Message
}
