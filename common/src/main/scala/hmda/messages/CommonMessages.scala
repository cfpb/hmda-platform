package hmda.messages

object CommonMessages {
  sealed trait Message

  trait Command
  trait Event

  final case object StopActor extends Command
}
