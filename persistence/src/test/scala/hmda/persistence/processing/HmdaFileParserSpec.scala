package hmda.persistence.processing

import java.time.Instant

import akka.actor.ActorRef
import akka.testkit.TestProbe
import org.scalatest.BeforeAndAfterEach
import com.typesafe.config.ConfigFactory
import hmda.model.fi.SubmissionId
import hmda.parser.fi.lar.LarParsingError
import hmda.parser.fi.ts.TsCsvParser
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.model.ActorSpec
import hmda.persistence.processing.HmdaFileParser._
import hmda.persistence.processing.HmdaRawFile._
import hmda.persistence.processing.ProcessingMessages._

class HmdaFileParserSpec extends ActorSpec with BeforeAndAfterEach with HmdaFileParserSpecUtils {
  import hmda.model.util.FITestData._

  val config = ConfigFactory.load()

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
      probe.expectMsg(HmdaFileParseState(0, Seq("Timestamp is not an integer"), Nil))
    }

    "persist parsed LARs" in {
      parseLars(hmdaFileParser, probe, lines)
      probe.send(hmdaFileParser, GetState)
      probe.expectMsg(HmdaFileParseState(3, Nil, Nil))
    }

    "persist parsed LARs and parsing errors" in {
      parseLars(hmdaFileParser, probe, badLines)
      probe.send(hmdaFileParser, GetState)
      probe.expectMsg(HmdaFileParseState(2, Nil, Seq(LarParsingError(0, List("Agency Code is not an integer")))))
    }

    "read entire raw file" in {
      val submissionId2 = SubmissionId("0", "2017", 2)
      val hmdaFileParser2 = createHmdaFileParser(system, submissionId2)
      val hmdaRawFile = createHmdaRawFile(system, submissionId2)
      for (line <- lines) {
        probe.send(hmdaRawFile, AddLine(timestamp, line.toString))
        probe.expectMsg(Persisted)
      }

      probe.send(hmdaRawFile, CompleteUpload)
      probe.expectMsg(UploadCompleted(4, submissionId2))

      probe.send(hmdaRawFile, GetState)
      probe.expectMsg(HmdaRawFileState(4))

      probe.send(hmdaFileParser2, ReadHmdaRawFile(s"${HmdaRawFile.name}-$submissionId2", probe.testActor))
      probe.expectMsg(ParsingCompleted(submissionId2))

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
        probe.expectMsg(Persisted)
      }

      probe.send(rawFileActor, CompleteUpload)
      probe.expectMsg(UploadCompleted(4, submissionId3))

      probe.send(rawFileActor, GetState)
      probe.expectMsg(HmdaRawFileState(4))

      probe.send(parserActor, ReadHmdaRawFile(s"${HmdaRawFile.name}-$submissionId3", probe.testActor))
      probe.expectMsg(ParsingCompletedWithErrors(submissionId3))

    }

    "get paginated results with GetStatePaginated(page)" in {
      // Setup: persist enough errors that pagination is necessary
      val tsErrors = List("TS 1", "TS 2")
      probe.send(hmdaFileParser, TsParsedErrors(tsErrors))
      1.to(42).foreach { i =>
        val err = LarParsingError(i, List(s"$i"))
        probe.send(hmdaFileParser, LarParsedErrors(err))
        probe.expectMsg(Persisted)
      }

      // First page should have TS errors and 19 LAR errors (20 rows' errors total)
      probe.send(hmdaFileParser, GetStatePaginated(1))
      val page1 = probe.expectMsgType[PaginatedFileParseState]
      page1.tsParsingErrors mustBe tsErrors
      page1.larParsingErrors.size mustBe 19
      page1.larParsingErrors.head.lineNumber mustBe 1

      // Second page should have 20 LAR errors
      probe.send(hmdaFileParser, GetStatePaginated(2))
      val page2 = probe.expectMsgType[PaginatedFileParseState]
      page2.tsParsingErrors mustBe Seq()
      page2.larParsingErrors.size mustBe 20
      page2.larParsingErrors.head.lineNumber mustBe 20

      // Third page should have the last 3 LAR errors
      probe.send(hmdaFileParser, GetStatePaginated(3))
      val page3 = probe.expectMsgType[PaginatedFileParseState]
      page3.tsParsingErrors mustBe Seq()
      page3.larParsingErrors.size mustBe 3
      page3.larParsingErrors.head.lineNumber mustBe 40
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
