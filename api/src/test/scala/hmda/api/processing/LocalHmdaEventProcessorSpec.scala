package hmda.api.processing

import akka.actor.ActorSystem
import akka.testkit.{ EventFilter, TestProbe }
import com.typesafe.config.ConfigFactory
import hmda.actor.test.ActorSpec
import hmda.api.processing.LocalHmdaEventProcessor._
import hmda.persistence.CommonMessages.Event
import hmda.persistence.processing.HmdaFileParser.ParsingCompleted
import hmda.persistence.processing.HmdaFileValidator.{ SyntacticalAndValidityCompleted, ValidationCompleted, ValidationCompletedWithErrors, ValidationStarted }
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
          | akka.log.dead-letters = off
          | akka.log-dead-letters-during-shutdown = off
          | """.stripMargin
      )
    )

  val probe = TestProbe()

  val eventProcessor = createLocalHmdaEventProcessor(system)

  "Event processor" must {
    val submissionId = "12345-2017-1"

    "process upload start message from event stream" in {
      val msg = s"Upload started for submission $submissionId"
      checkEventStreamMessage(msg, UploadStarted(submissionId))
    }

    "process upload completed message from event stream" in {
      val size = 10
      val msg = s"$size lines uploaded for submission $submissionId"
      checkEventStreamMessage(msg, UploadCompleted(size, submissionId))
    }

    "process parse completed message from event stream" in {
      val msg = s"Parsing completed for $submissionId"
      checkEventStreamMessage(msg, ParsingCompleted(submissionId))
    }

    "process validation started message from event stream" in {
      val msg = s"Validation started for $submissionId"
      checkEventStreamMessage(msg, ValidationStarted(submissionId))
    }

    "process syntactical and validity completed message from event stream" in {
      val msg = s"Submission $submissionId contains syntactical and / or validity errors"
      checkEventStreamMessage(msg, SyntacticalAndValidityCompleted(submissionId))
    }

    "process validation completed with errors from event stream" in {
      val msg = s"validation completed with errors for submission $submissionId"
      checkEventStreamMessage(msg, ValidationCompletedWithErrors(submissionId))
    }

    "process validation completed from event stream" in {
      val msg = s"validation completed for submission $submissionId"
      checkEventStreamMessage(msg, ValidationCompleted(submissionId))
    }
  }

  private def checkEventStreamMessage(msg: String, event: Event): Unit = {
    val actorSource = eventProcessor.path.toString
    EventFilter.debug(msg, source = actorSource) intercept {
      system.eventStream.publish(event)
    }
  }
}
