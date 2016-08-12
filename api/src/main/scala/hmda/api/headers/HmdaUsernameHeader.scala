package hmda.api.headers

import akka.http.scaladsl.model.headers.{ ModeledCustomHeader, ModeledCustomHeaderCompanion }
import scala.util.Try

final class HmdaUsernameHeader(username: String) extends ModeledCustomHeader[HmdaUsernameHeader] {
  override def companion: ModeledCustomHeaderCompanion[HmdaUsernameHeader] = HmdaUsernameHeader
  override def value(): String = username
  override def renderInResponses(): Boolean = false
  override def renderInRequests(): Boolean = true
}

object HmdaUsernameHeader extends ModeledCustomHeaderCompanion[HmdaUsernameHeader] {
  override def name: String = "CFPB-HMDA-Username"
  override def parse(value: String): Try[HmdaUsernameHeader] = Try(new HmdaUsernameHeader(value))
}
