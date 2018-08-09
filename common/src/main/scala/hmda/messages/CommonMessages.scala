package hmda.messages

object CommonMessages {
  sealed trait Message
  final case object StopActor extends Message
}
