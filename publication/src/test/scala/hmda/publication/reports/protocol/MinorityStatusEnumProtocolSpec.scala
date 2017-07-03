package hmda.publication.reports.protocol

import hmda.model.publication.reports.MinorityStatusEnum
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.publication.reports.ReportGenerators._
import spray.json._

class MinorityStatusEnumProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with MinorityStatusEnumProtocol {

  forAll(minorityStatusEnumGen) { m =>
    m.toJson.convertTo[MinorityStatusEnum] mustBe m
  }
}
