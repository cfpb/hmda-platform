package hmda.persistence.processing

import java.io.File
import java.time.Instant

import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import hmda.actor.test.ActorSpec
import hmda.parser.fi.lar.LarCsvParser
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

  val lines = fiCSV.split("\n")
  val timestamp = Instant.now.toEpochMilli

  "HMDA File Parser" must {
    "parse raw data stored in the event journal" in {
      probe.send(hmdaFileParser, ReadHmdaRawFile(s"${HmdaRawFile.name}-$submissionId"))
      //probe.expectMsg("hello")
      //1 === 1
    }
  }

  val snapshotStore = new File(config.getString("akka.persistence.snapshot-store.local.dir"))

  override def beforeAll() {
    FileUtils.deleteRecursively(snapshotStore)
    persistRawData()
    super.beforeAll()
  }

  private def persistRawData(): Unit = {
    for (line <- lines) {
      probe.send(hmdaFileUpload, AddLine(timestamp, line.toString))
    }
  }

}
