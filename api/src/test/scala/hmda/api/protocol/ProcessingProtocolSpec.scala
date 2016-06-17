package hmda.api.protocol

import org.scalatest._
import prop._
import hmda.api.model.ModelGenerators
import hmda.api.model.processing.{ Institution, Institutions, ProcessingStatus }
import hmda.api.protocol.processing.ProcessingProtocol
import spray.json._

class ProcessingProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with ModelGenerators with ProcessingProtocol {

  property("Processing status must convert to and from json") {
    forAll(processingStatusGen) { p =>
      p.toJson.convertTo[ProcessingStatus] mustBe (p)
    }
  }

  property("An Institution filing must convert to and from json") {
    forAll(institutionGen) { i =>
      i.toJson.convertTo[Institution] mustBe (i)
    }
  }

  property("Institutions must convert to and from json") {
    forAll(institutionsGen) { xs =>
      xs.toJson.convertTo[Institutions] mustBe (xs)
    }
  }
}
