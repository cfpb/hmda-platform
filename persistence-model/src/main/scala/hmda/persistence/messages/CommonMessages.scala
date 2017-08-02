package hmda.persistence.messages

object CommonMessages {
  trait Command
  trait Event
  case object Shutdown
  case object GetState
}
