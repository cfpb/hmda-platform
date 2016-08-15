package hmda.api.headers

import akka.http.scaladsl.model.headers.{ ModeledCustomHeader, ModeledCustomHeaderCompanion }

import scala.util.Try

final class HmdaInstitutionsHeader(ids: List[String]) extends ModeledCustomHeader[HmdaInstitutionsHeader] {
  override def companion: ModeledCustomHeaderCompanion[HmdaInstitutionsHeader] = HmdaInstitutionsHeader
  override def value(): String = ids.mkString(",")
  override def renderInResponses(): Boolean = false
  override def renderInRequests(): Boolean = true
}

object HmdaInstitutionsHeader extends ModeledCustomHeaderCompanion[HmdaInstitutionsHeader] {
  override def name: String = "CFPB-HMDA-Institutions"
  override def parse(ids: String): Try[HmdaInstitutionsHeader] = {
    val idList = ids.split(",").toList
    Try(new HmdaInstitutionsHeader(idList))
  }
}
