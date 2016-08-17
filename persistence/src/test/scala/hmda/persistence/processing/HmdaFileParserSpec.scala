package hmda.persistence.processing

import java.time.Instant

import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import hmda.actor.test.ActorSpec
import hmda.parser.fi.lar.LarCsvParser
import hmda.parser.fi.ts.TsCsvParser
import hmda.persistence.CommonMessages.GetState
import hmda.persistence.processing.HmdaRawFile._
import hmda.persistence.processing.HmdaFileParser._

import scalaz.Alpha.H

class HmdaFileParserSpec extends ActorSpec {
  import hmda.model.util.FITestData._

  val config = ConfigFactory.load()

  val submissionId = "12345-2017-1"

  val hmdaFileParser = createHmdaFileParser(system, submissionId)

  val probe = TestProbe()

  val timestamp = Instant.now.toEpochMilli
  val lines = fiCSV.split("\n")
  val badLines = fiCSVParseError.split("\n")

  "HMDA File Parser" must {
    "persist parsed TSs" in {
      parseTs(lines)
      probe.send(hmdaFileParser, GetState)
      probe.expectMsg(HmdaFileParseState(1, Nil))
    }

    "persist parsed TSs and TS parsing errors" in {
      parseTs(badLines)
      probe.send(hmdaFileParser, GetState)
      probe.expectMsg(HmdaFileParseState(1, Seq(List("Timestamp is not a Long"))))
    }

    "persist parsed LARs" in {
      parseLars(lines)
      probe.send(hmdaFileParser, GetState)
      probe.expectMsg(HmdaFileParseState(4, Seq(List("Timestamp is not a Long"))))
    }

    "persist parsed LARs and parsing errors" in {
      parseLars(badLines)
      probe.send(hmdaFileParser, GetState)
      probe.expectMsg(HmdaFileParseState(6, Seq(List("Timestamp is not a Long"), List("Agency Code is not an Integer"))))
    }

    "read entire raw file" in {
      probe.send(hmdaFileParser, ReadHmdaRawFile)
      probe.send(hmdaFileParser, GetState)
      probe.expectMsg(HmdaFileParseState(6, Seq(List("Timestamp is not a Long"), List("Agency Code is not an Integer"))))
    }
  }

  private def parseTs(xs: Array[String]): Array[Unit] = {
    val lars = xs.take(1).map(line => TsCsvParser(line))
    lars.map {
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
