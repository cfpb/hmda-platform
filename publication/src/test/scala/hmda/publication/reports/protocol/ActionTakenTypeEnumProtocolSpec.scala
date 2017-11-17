package hmda.publication.reports.protocol

import hmda.model.publication.reports.DispositionEnum
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.publication.reports.ReportGenerators._
import spray.json._

class ActionTakenTypeEnumProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with ActionTakenTypeEnumProtocol {

  property("Action Taken Type must convert to and from JSON") {
    forAll(actionTakenTypeEnumGen) { a =>
      a.toJson.convertTo[DispositionEnum] mustBe a
    }
  }

}
