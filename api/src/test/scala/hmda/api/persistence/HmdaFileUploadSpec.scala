package hmda.api.persistence

import java.io.File
import java.time.Instant

import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import hmda.api.persistence.CommonMessages._
import hmda.api.persistence.HmdaFileUpload.{ AddLine, CompleteUpload, HmdaFileUploadState }
import hmda.api.persistence.HmdaFileUpload._
import hmda.api.processing.ActorSpec
import org.iq80.leveldb.util.FileUtils

class HmdaFileUploadSpec extends ActorSpec {
  import hmda.parser.util.FITestData._

  val config = ConfigFactory.load()

  val hmdaFileUpload = createHmdaFileUpload(system, "1")

  val probe = TestProbe()

  val lines = fiCSV.split("\n")
  val timestamp = Instant.now.toEpochMilli

  "A HMDA File" must {
    "be persisted" in {
      for (line <- lines) {
        probe.send(hmdaFileUpload, AddLine(timestamp, line.toString))
      }
      probe.send(hmdaFileUpload, GetState)
      probe.expectMsg(HmdaFileUploadState(Map(timestamp -> 4)))
    }

    "recover with event" in {
      probe.send(hmdaFileUpload, Shutdown)

      val secondHmdaFileUpload = createHmdaFileUpload(system, "1")

      probe.send(secondHmdaFileUpload, GetState)
      probe.expectMsg(HmdaFileUploadState(Map(timestamp -> 4)))
      probe.send(secondHmdaFileUpload, Shutdown)
    }

    "recover with from snapshot" in {
      val thirdHmdaFileUpload = createHmdaFileUpload(system, "1")
      probe.send(thirdHmdaFileUpload, CompleteUpload)
      probe.send(thirdHmdaFileUpload, Shutdown)

      Thread.sleep(500) //wait for actor messages to be processed so that the state can be saved

      val fourthHmdaFileUpload = createHmdaFileUpload(system, "1")

      probe.send(fourthHmdaFileUpload, GetState)
      probe.expectMsg(HmdaFileUploadState(Map(timestamp -> 4)))
    }
  }

  val snapshotStore = new File(config.getString("akka.persistence.snapshot-store.local.dir"))

  override def beforeAll() {
    FileUtils.deleteRecursively(snapshotStore)
    super.beforeAll()
  }
}
