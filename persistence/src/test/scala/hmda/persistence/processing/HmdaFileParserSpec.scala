package hmda.persistence.processing

import java.time.Instant

import akka.actor.ActorRef
import akka.testkit.TestProbe
import org.scalatest.BeforeAndAfterEach
import com.typesafe.config.ConfigFactory
import hmda.actor.test.ActorSpec
import hmda.parser.fi.lar.LarCsvParser
import hmda.parser.fi.ts.TsCsvParser
import hmda.persistence.CommonMessages.GetState
import hmda.persistence.processing.HmdaFileParser._
import hmda.persistence.processing.HmdaRawFile._

class HmdaFileParserSpec extends ActorSpec with BeforeAndAfterEach {
  import hmda.model.util.FITestData._

  val config = ConfigFactory.load()

  val submissionId = "12345-2017-1"

  var hmdaFileParser: ActorRef = _
  val probe = TestProbe()

  val timestamp = Instant.now.toEpochMilli
  val lines = fiCSV.split("\n")
  val badLines = fiCSVParseError.split("\n")

  override def beforeEach(): Unit = {
    hmdaFileParser = createHmdaFileParser(system, submissionId + Instant.now.toEpochMilli)
  }

  "HMDA File Parser" must {
    "persist parsed TSs" in {
      parseTs(lines)
      probe.send(hmdaFileParser, GetState)
      probe.expectMsg(HmdaFileParseState(1, Nil))
    }

    "persist TS parsing errors" in {
      parseTs(badLines)
      probe.send(hmdaFileParser, GetState)
      probe.expectMsg(HmdaFileParseState(0, Seq(List("Timestamp is not a Long"))))
    }

    "persist parsed LARs" in {
      parseLars(lines)
      probe.send(hmdaFileParser, GetState)
      probe.expectMsg(HmdaFileParseState(3, Nil))
    }

    "persist parsed LARs and parsing errors" in {
      parseLars(badLines)
      probe.send(hmdaFileParser, GetState)
      probe.expectMsg(HmdaFileParseState(2, Seq(List("Agency Code is not an Integer"))))
    }

    "read entire raw file" in {
      val hmdaFileParser2 = createHmdaFileParser(system, "12345-2017-2")
      val hmdaRawFile = createHmdaRawFile(system, "12345-2017-2")
      for (line <- lines) {
        probe.send(hmdaRawFile, AddLine(timestamp, line.toString))
      }
      probe.send(hmdaRawFile, GetState)
      probe.expectMsg(HmdaRawFileState(4))

      probe.send(hmdaFileParser2, ReadHmdaRawFile("HmdaRawFile-" + "12345-2017-2"))
      Thread.sleep(2000)
      probe.send(hmdaFileParser2, GetState)
      probe.expectMsg(HmdaFileParseState(4, Nil))
    }
  }

  private def parseTs(xs: Array[String]): Array[Unit] = {
    val ts = xs.take(1).map(line => TsCsvParser(line))
    ts.map {
      case Right(ts) => probe.send(hmdaFileParser, TsParsed(ts))
      case Left(errors) => probe.send(hmdaFileParser, TsParsedErrors(errors))
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
