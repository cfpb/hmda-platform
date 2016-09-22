package hmda.api.processing

import akka.actor.{ ActorRef, ActorSystem }
import akka.testkit.{ EventFilter, TestProbe }
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent._
import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory
import hmda.actor.test.ActorSpec
import hmda.model.fi._
import hmda.persistence.CommonMessages.{ Event, GetState }
import hmda.persistence.processing.HmdaFileParser.{ ParsingCompleted, ParsingCompletedWithErrors, ParsingStarted }
import hmda.persistence.processing.HmdaFileValidator.{ ValidationCompleted, ValidationCompletedWithErrors, ValidationStarted }
import hmda.persistence.processing.HmdaRawFile.{ UploadCompleted, UploadStarted }
import hmda.persistence.HmdaSupervisor._
import hmda.persistence.institutions.SubmissionPersistence
import hmda.persistence.processing.LocalHmdaEventProcessor
import hmda.persistence.institutions.SubmissionPersistence._
import org.scalatest.Assertion

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

  val duration = 5.seconds
  implicit val timeout = Timeout(duration)
  implicit val ec = system.dispatcher

  val probe = TestProbe()

  val supervisor = createSupervisor(system)
  val fEventProcessor = (supervisor ? FindActorByName(LocalHmdaEventProcessor.name)).mapTo[ActorRef]
  val eventProcessor = Await.result(fEventProcessor, duration)

  val submissionId = SubmissionId("0", "2017", 1)

  override def beforeAll(): Unit = {
    super.beforeAll()
    val fSubmissions = (supervisor ? FindSubmissions(SubmissionPersistence.name, submissionId.institutionId, submissionId.period)).mapTo[ActorRef]
    for {
      s <- fSubmissions
    } yield {
      s ! CreateSubmission
    }
  }

  "Event processor" must {

    "process upload start message from event stream" in {
      val msg = s"Upload started for submission $submissionId"
      val status = UploadStarted(submissionId)
      checkEventStreamMessage(msg, status)
      checkSubmissionStatus(Uploading)
    }

    "process upload completed message from event stream" in {
      val size = 10
      val msg = s"$size lines uploaded for submission $submissionId"
      checkEventStreamMessage(msg, UploadCompleted(size, submissionId))
    }

    "process parse started message from event stream" in {
      val msg = s"Parsing started for submission $submissionId"
      checkEventStreamMessage(msg, ParsingStarted(submissionId))
    }

    "process parse completed message from event stream" in {
      val msg = s"Parsing completed for $submissionId"
      checkEventStreamMessage(msg, ParsingCompleted(submissionId))
    }

    "process 'parsingCompletedWithErrors' message from event stream" in {
      val msg = s"Parsing completed with errors for submission $submissionId"
      checkEventStreamMessage(msg, ParsingCompletedWithErrors(submissionId))
      //checkSubmissionStatus(ParsedWithErrors)
      // TODO: improve checkSubmissionStatus so that this test passes consistently
      //   and we can add checkSubmissionStatus to all specs in this file
    }

    "process validation started message from event stream" in {
      val msg = s"Validation started for $submissionId"
      checkEventStreamMessage(msg, ValidationStarted(submissionId))
    }

    "process validation completed with errors from event stream" in {
      val msg = s"validation completed with errors for submission $submissionId"
      checkEventStreamMessage(msg, ValidationCompletedWithErrors(submissionId))
    }

    "process validation completed from event stream" in {
      val msg = s"Validation completed for submission $submissionId"
      checkEventStreamMessage(msg, ValidationCompleted(submissionId))
      checkSubmissionStatus(Validated)
    }
  }

  def checkSubmissionStatus(status: SubmissionStatus): Assertion = {
    //TODO: find a way to avoid having to sleep here
    Thread.sleep(500)
    val fSubmissions = (supervisor ? FindSubmissions(SubmissionPersistence.name, submissionId.institutionId, submissionId.period)).mapTo[ActorRef]
    val fSubmissionStatus = for {
      a <- fSubmissions
      b <- (a ? GetState).mapTo[Seq[Submission]]
    } yield b.head.submissionStatus

    val submissionStatus = Await.result(fSubmissionStatus, duration)
    submissionStatus mustBe status
  }

  private def checkEventStreamMessage(msg: String, event: Event): Unit = {
    val actorSource = eventProcessor.path.toString
    EventFilter.debug(msg, source = actorSource, occurrences = 1) intercept {
      system.eventStream.publish(event)
    }
  }

  override def afterAll(): Unit = {
    Thread.sleep(2000)
    system.terminate()
  }
}
