package hmda.persistence.processing

import java.io.File
import java.time.Instant

import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import hmda.actor.test.ActorSpec
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.parser.fi.lar.LarCsvParser
import hmda.persistence.CommonMessages.GetState
import hmda.persistence.processing.HmdaRawFile._
import hmda.persistence.processing.HmdaFileParser._
import org.iq80.leveldb.util.FileUtils

class HmdaFileParserSpec extends ActorSpec {
  import hmda.model.util.FITestData._

  val config = ConfigFactory.load()

  val submissionId = "12345-2017-1"

  val hmdaFileUpload = createHmdaRawFile(system, submissionId)

  val hmdaFileParser = createHmdaFileParser(system, submissionId)

  val probe = TestProbe()

  val timestamp = Instant.now.toEpochMilli
  val lines = fiCSV.split("\n")
  val badLines = fiCSVParseError.split("\n")

  override def beforeAll() {
    persistRawData()
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

  private def persistRawData(): Unit = {
    for (line <- lines) {
      probe.send(hmdaFileUpload, AddLine(timestamp, line.toString))
    }
  }

}
