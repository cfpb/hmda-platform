package hmda.api.processing.submission

object CommonMessages {
  trait Command
  trait Event
  case object Shutdown
  case object GetState
}