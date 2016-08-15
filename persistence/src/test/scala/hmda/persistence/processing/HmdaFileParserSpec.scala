package hmda.persistence.processing

import java.time.Instant

import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import hmda.actor.test.ActorSpec
import hmda.parser.fi.lar.LarCsvParser
import hmda.persistence.CommonMessages.GetState
import hmda.persistence.processing.HmdaRawFile._
import hmda.persistence.processing.HmdaFileParser._

class HmdaFileParserSpec extends ActorSpec {
  import hmda.model.util.FITestData._

  val config = ConfigFactory.load()

  val submissionId = "12345-2017-1"

  val hmdaFileParser = createHmdaFileParser(system, submissionId)

  val probe = TestProbe()

  val timestamp = Instant.now.toEpochMilli
  val lines = fiCSV.split("\n")
  val badLines = fiCSVParseError.split("\n")

  override def beforeAll() {
    super.beforeAll()
  }

  "HMDA File Parser" must {
    "persist parsed LARs" in {
      parseLars(lines)
      probe.send(hmdaFileParser, GetState)
      probe.expectMsg(HmdaFileParseState(3, Nil))
    }

    "persist parsed LARs and parsing errors" in {
      parseLars(badLines)
      probe.send(hmdaFileParser, GetState)
      probe.expectMsg(HmdaFileParseState(5, Seq(List("Agency Code is not an Integer"))))

    }
  }

  private def parseLars(xs: Array[String]): Array[Unit] = {
    val lars = xs.drop(1).map(line => LarCsvParser(line))
    lars.map {
      case Right(l) => probe.send(hmdaFileParser, LarParsed(l))
      case Left(errors) => probe.send(hmdaFileParser, LarParsedErrors(errors))
    }
  }

}
