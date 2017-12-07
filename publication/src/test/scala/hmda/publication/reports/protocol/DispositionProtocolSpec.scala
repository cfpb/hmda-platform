package hmda.publication.reports.protocol

import hmda.model.publication.reports.ValueDisposition
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.publication.reports.ReportGenerators._
import spray.json._

class DispositionProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with DispositionProtocol {
  property("Disposition must convert to and from JSON") {
    forAll(valueDispositionGen) { d =>
      d.toJson.convertTo[ValueDisposition] mustBe d
    }
  }

}
