package hmda.api.protocol

import scala.concurrent.ExecutionContext
import org.scalatest._
import prop._
import hmda.api.model.{ Status, ModelGenerators }
import spray.json._

class HmdaApiProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with ModelGenerators with HmdaApiProtocol {

  property("status must convert to and from json") {
    forAll(statusGen) { (s) =>
      s.toJson.convertTo[Status] mustBe (s)
    }

  }

}
