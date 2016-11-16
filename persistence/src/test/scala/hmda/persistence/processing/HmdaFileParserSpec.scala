package hmda.persistence.processing

import java.time.Instant

import akka.actor.{ ActorRef, ActorSystem }
import akka.testkit.{ EventFilter, TestProbe }
import org.scalatest.BeforeAndAfterEach
import com.typesafe.config.ConfigFactory
import hmda.model.fi.SubmissionId
import hmda.parser.fi.lar.LarParsingError
import hmda.parser.fi.ts.TsCsvParser
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.model.ActorSpec
import hmda.persistence.processing.HmdaFileParser._
import hmda.persistence.processing.HmdaRawFile._

class HmdaFileParserSpec extends ActorSpec with BeforeAndAfterEach with HmdaFileParserSpecUtils {
  import hmda.model.util.FITestData._

  val config = ConfigFactory.load()
  override implicit lazy val system =
    ActorSystem(
      "test-system",
      ConfigFactory.parseString(
        TestConfigOverride.config
      )
    )

  val submissionId = SubmissionId("0", "2017", 1)

  var hmdaFileParser: ActorRef = _
  val probe = TestProbe()

  val timestamp = Instant.now.toEpochMilli
  val lines = fiCSV.split("\n")
  val badLines = fiCSVParseError.split("\n")

  override def beforeEach(): Unit = {
    val seqNr = submissionId.sequenceNumber + Instant.now.toEpochMilli.toInt
    val id = submissionId.copy(sequenceNumber = seqNr)
    hmdaFileParser = createHmdaFileParser(system, id)
  }

  "HMDA File Parser" must {
    "persist parsed TSs" in {
      parseTs(lines)
      probe.send(hmdaFileParser, GetState)
      probe.expectMsg(HmdaFileParseState(1, Nil, Nil))
    }

    "persist TS parsing errors" in {
      parseTs(badLines)
      probe.send(hmdaFileParser, GetState)
      probe.expectMsg(HmdaFileParseState(0, Seq("Timestamp is not a Long"), Nil))
    }

    "persist parsed LARs" in {
      parseLars(hmdaFileParser, probe, lines)
      probe.send(hmdaFileParser, GetState)
      probe.expectMsg(HmdaFileParseState(3, Nil, Nil))
    }

    "persist parsed LARs and parsing errors" in {
      parseLars(hmdaFileParser, probe, badLines)
      probe.send(hmdaFileParser, GetState)
      probe.expectMsg(HmdaFileParseState(2, Nil, Seq(LarParsingError(0, List("Agency Code is not an Integer")))))
    }

    "read entire raw file" in {
      val submissionId2 = SubmissionId("0", "2017", 2)
      val hmdaFileParser2 = createHmdaFileParser(system, submissionId2)
      val hmdaRawFile = createHmdaRawFile(system, submissionId2)
      for (line <- lines) {
        probe.send(hmdaRawFile, AddLine(timestamp, line.toString))
      }
      probe.send(hmdaRawFile, GetState)
      probe.expectMsg(HmdaRawFileState(4))

      val msg = "Parsing completed for 0-2017-2"
      EventFilter.debug(msg, source = hmdaFileParser2.path.toString, occurrences = 1) intercept {
        probe.send(hmdaFileParser2, ReadHmdaRawFile("HmdaRawFile-" + "0-2017-2"))
      }

      probe.send(hmdaFileParser2, GetState)
      probe.expectMsg(HmdaFileParseState(4, Nil))
    }

    "send 'ParsedWithErrors' when TS parsing fails" in {
      val submissionId3 = SubmissionId("0", "2017", 3)
      val parserActor = createHmdaFileParser(system, submissionId3)
      val rawFileActor = createHmdaRawFile(system, submissionId3)

      // setup: persist raw lines
      for (line <- badLines) {
        probe.send(rawFileActor, AddLine(timestamp, line))
      }
      probe.send(rawFileActor, GetState)
      probe.expectMsg(HmdaRawFileState(4))

      // test: parse those lines, expect "ParsedWithErrors" message
      val msg = s"Parsing completed for $submissionId3, errors found"
      EventFilter.debug(msg, source = parserActor.path.toString, occurrences = 1) intercept {
        probe.send(parserActor, ReadHmdaRawFile(s"HmdaRawFile-$submissionId3"))
      }
    }

  }

  private def parseTs(xs: Array[String]): Unit = {
    val ts = xs.take(1).map(line => TsCsvParser(line))
    ts.foreach {
      case Right(parsedTs) => probe.send(hmdaFileParser, TsParsed(parsedTs))
      case Left(errors) => probe.send(hmdaFileParser, TsParsedErrors(errors))
    }
  }

}
