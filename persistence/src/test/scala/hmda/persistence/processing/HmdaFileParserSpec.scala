package hmda.persistence.processing

import java.time.Instant

import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import hmda.actor.test.ActorSpec
import hmda.persistence.CommonMessages.GetState
import hmda.persistence.processing.HmdaFileParser._

class HmdaFileParserSpec extends ActorSpec with HmdaFileParserSpecUtils {
  import hmda.model.util.FITestData._

  val config = ConfigFactory.load()

  val submissionId = "12345-2017-1"

  val hmdaFileParser = createHmdaFileParser(system, submissionId)

  val probe = TestProbe()

  val timestamp = Instant.now.toEpochMilli
  val lines = fiCSV.split("\n")
  val badLines = fiCSVParseError.split("\n")

  "HMDA File Parser" must {
    "persist parsed LARs" in {
      parseLars(hmdaFileParser, probe, lines)
      probe.send(hmdaFileParser, GetState)
      probe.expectMsg(HmdaFileParseState(3, Nil))
    }

    "persist parsed LARs and parsing errors" in {
      parseLars(hmdaFileParser, probe, badLines)
      probe.send(hmdaFileParser, GetState)
      probe.expectMsg(HmdaFileParseState(5, Seq(List("Agency Code is not an Integer"))))

    }
  }

}
