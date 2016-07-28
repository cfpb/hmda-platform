package hmda.persistence.processing

import java.io.File
import java.time.Instant

import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import hmda.actor.test.ActorSpec
import hmda.persistence.CommonMessages._
import hmda.persistence.processing.HmdaRawFile._
import org.iq80.leveldb.util.FileUtils

class HmdaRawFileSpec extends ActorSpec {
  import hmda.model.util.FITestData._

  val config = ConfigFactory.load()

  val hmdaFileUpload = createHmdaRawFile(system, "1")

  val probe = TestProbe()

  val lines = fiCSV.split("\n")
  val timestamp = Instant.now.toEpochMilli
  val numLines = lines.size

  "A HMDA File" must {
    "be persisted" in {
      for (line <- lines) {
        probe.send(hmdaFileUpload, AddLine(timestamp, line.toString))
      }
      probe.send(hmdaFileUpload, GetState)
      probe.expectMsg(HmdaRawFileState(numLines))
    }

    "recover with event" in {
      probe.send(hmdaFileUpload, Shutdown)

      val secondHmdaFileUpload = createHmdaRawFile(system, "1")

      probe.send(secondHmdaFileUpload, GetState)
      probe.expectMsg(HmdaRawFileState(numLines))
      probe.send(secondHmdaFileUpload, Shutdown)
    }

    "recover with from snapshot" in {
      val thirdHmdaFileUpload = createHmdaRawFile(system, "1")
      probe.send(thirdHmdaFileUpload, UploadCompleted)
      probe.send(thirdHmdaFileUpload, Shutdown)

      Thread.sleep(500) //wait for actor messages to be processed so that the state can be saved

      val fourthHmdaFileUpload = createHmdaRawFile(system, "1")

      probe.send(fourthHmdaFileUpload, GetState)
      probe.expectMsg(HmdaRawFileState(numLines))
    }
  }

  val snapshotStore = new File(config.getString("akka.persistence.snapshot-store.local.dir"))

  override def beforeAll() {
    FileUtils.deleteRecursively(snapshotStore)
    super.beforeAll()
  }
}
