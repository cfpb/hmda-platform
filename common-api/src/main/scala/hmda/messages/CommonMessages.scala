package hmda.messages

object CommonMessages {
  sealed trait Message
  case object StopActor extends Message
}
