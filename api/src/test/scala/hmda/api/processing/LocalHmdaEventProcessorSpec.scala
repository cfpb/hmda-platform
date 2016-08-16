package hmda.api.processing

import akka.actor.ActorSystem
import akka.testkit.{ EventFilter, TestProbe }
import com.typesafe.config.ConfigFactory
import hmda.actor.test.ActorSpec
import hmda.api.processing.LocalHmdaEventProcessor._
import hmda.persistence.processing.HmdaFileParser.ParsingCompleted
import hmda.persistence.processing.HmdaRawFile.{ UploadCompleted, UploadStarted }

class LocalHmdaEventProcessorSpec extends ActorSpec {

  override implicit lazy val system =
    ActorSystem(
      "test-system",
      ConfigFactory.parseString(
        """
          | akka.loggers = ["akka.testkit.TestEventListener"]
          | akka.loglevel = DEBUG
          | akka.stdout-loglevel = "OFF"
          | akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
          | akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot-store.local"
          | akka.persistence.snapshot-store.local.dir = "target/snapshots"
          | """.stripMargin
      )
    )

  val probe = TestProbe()

  val eventProcessor = createLocalHmdaEventProcessor(system)

  "Event processor" must {
    val submissionId = "12345-2017-1"
    val actorSource = eventProcessor.path.toString
    "process upload start message from event stream" in {
      val msg = s"Upload started for submission $submissionId"
      EventFilter.debug(msg, source = actorSource, occurrences = 1) intercept {
        system.eventStream.publish(UploadStarted(submissionId))
      }
    }
    "process upload completed message from event stream" in {
      val size = 10
      val msg = s"$size lines uploaded for submission $submissionId"
      EventFilter.debug(msg, source = actorSource, occurrences = 1) intercept {
        system.eventStream.publish(UploadCompleted(size, submissionId))
      }
    }

    "process parse completed message from event stream" in {
      val msg = s"Parsing completed for $submissionId"
      EventFilter.debug(msg, source = actorSource, occurrences = 1) intercept {
        system.eventStream.publish(ParsingCompleted(submissionId))
      }
    }
  }

}
