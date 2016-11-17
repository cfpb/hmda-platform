package hmda.persistence.processing

import akka.actor.{ ActorRef, ActorSystem }
import akka.pattern.ask
import akka.testkit.{ EventFilter, TestProbe }
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.model.fi._
import hmda.persistence.messages.CommonMessages.{ Event, GetState }
import hmda.persistence.HmdaSupervisor._
import hmda.persistence.institutions.SubmissionPersistence
import hmda.persistence.institutions.SubmissionPersistence._
import hmda.persistence.model.ActorSpec
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import hmda.persistence.processing.HmdaFileValidator.ValidationStarted
import hmda.persistence.processing.ProcessingMessages._
import org.scalatest.Assertion

import scala.concurrent._
import scala.concurrent.duration._

class LocalHmdaEventProcessorSpec extends ActorSpec {

  override implicit lazy val system =
    ActorSystem(
      "test-system",
      ConfigFactory.parseString(
        TestConfigOverride.config
      )
    )

  val duration = 10.seconds
  implicit val timeout = Timeout(duration)
  implicit val ec = system.dispatcher

  val probe = TestProbe()

  val supervisor = createSupervisor(system)
  val fEventProcessor = (supervisor ? FindActorByName(LocalHmdaEventProcessor.name)).mapTo[ActorRef]
  val eventProcessor = Await.result(fEventProcessor, duration)

  val submissionId = SubmissionId("testEvents1", "2017", 1)

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

    "process UploadStarted message from event stream" in {
      val msg = s"Upload started for submission $submissionId"
      val status = UploadStarted(submissionId)
      checkEventStreamMessage(msg, status)
      checkSubmissionStatus(Uploading)
    }

    "process UploadCompleted message from event stream" in {
      val submissionId = SubmissionId("testUploadComp", "2017", 1)
      val size = 10
      val msg = s"$size lines uploaded for submission $submissionId"
      checkEventStreamMessage(msg, UploadCompleted(size, submissionId))
    }

    "process ParsingStarted message from event stream" in {
      val msg = s"Parsing started for submission $submissionId"
      checkEventStreamMessage(msg, ParsingStarted(submissionId))
      checkSubmissionStatus(Parsing)
    }

    "process ParsingCompleted message from event stream" in {
      val submissionId = SubmissionId("testParseComp", "2017", 1)
      val msg = s"Parsing completed for $submissionId"
      checkEventStreamMessage(msg, ParsingCompleted(submissionId))
    }

    "process ParsingCompletedWithErrors message from event stream" in {
      val msg = s"Parsing completed with errors for submission $submissionId"
      checkEventStreamMessage(msg, ParsingCompletedWithErrors(submissionId))
      checkSubmissionStatus(ParsedWithErrors)
    }

    "process ValidationStarted message from event stream" in {
      val msg = s"Validation started for $submissionId"
      checkEventStreamMessage(msg, ValidationStarted(submissionId))
      checkSubmissionStatus(Validating)
    }

    "process ValidationCompletedWithErrors from event stream" in {
      val msg = s"validation completed with errors for submission $submissionId"
      checkEventStreamMessage(msg, ValidationCompletedWithErrors(submissionId))
      checkSubmissionStatus(ValidatedWithErrors)
    }

    "process ValidationCompleted from event stream" in {
      val msg = s"Validation completed for submission $submissionId"
      checkEventStreamMessage(msg, ValidationCompleted(submissionId))
      checkSubmissionStatus(Validated)
    }
  }

  private def checkSubmissionStatus(status: SubmissionStatus): Assertion = {
    val fSubmissions = (supervisor ? FindSubmissions(SubmissionPersistence.name, submissionId.institutionId, submissionId.period)).mapTo[ActorRef]
    val subActor = Await.result(fSubmissions, duration)
    val submissionSeq = Await.result((subActor ? GetState).mapTo[Seq[Submission]], duration)
    submissionSeq.head.status mustBe status
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
