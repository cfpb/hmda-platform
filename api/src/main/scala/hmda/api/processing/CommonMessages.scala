package hmda.api.processing

object CommonMessages {
  trait Command
  trait Event
  case object Shutdown
  case object GetState
}