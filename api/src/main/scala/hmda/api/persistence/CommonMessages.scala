package hmda.api.persistence

object CommonMessages {
  trait Command
  trait Event
  case object Shutdown
  case object GetState
}
