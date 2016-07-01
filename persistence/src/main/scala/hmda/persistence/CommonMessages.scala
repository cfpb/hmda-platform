package hmda.persistence

object CommonMessages {
  trait Command
  trait Event
  case object Shutdown
  case object GetState
}
