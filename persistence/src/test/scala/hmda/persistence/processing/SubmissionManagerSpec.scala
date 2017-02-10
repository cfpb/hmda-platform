package hmda.persistence.processing

import java.time.Instant

import akka.actor.ActorRef
import akka.testkit.TestProbe
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.model.fi._
import hmda.model.util.FITestData._
import hmda.persistence.HmdaSupervisor
import hmda.persistence.HmdaSupervisor.{ FindFilings, FindProcessingActor }
import hmda.persistence.institutions.FilingPersistence
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.model.ActorSpec
import hmda.persistence.processing.ProcessingMessages.{ CompleteUpload, StartUpload }
import hmda.persistence.institutions.FilingPersistence._
import hmda.persistence.processing.HmdaRawFile.AddLine

import scala.concurrent.Await
import scala.concurrent.duration._

class SubmissionManagerSpec extends ActorSpec {
  val timeout = 2.seconds
  implicit val akkaTimeout = Timeout(timeout)

  val config = ConfigFactory.load()

  val submissionId = SubmissionId("0", "testPeriod", 1)

  val supervisor = system.actorOf(HmdaSupervisor.props(), "supervisor")
  val fFilingPersistence = (supervisor ? FindFilings(FilingPersistence.name, submissionId.institutionId)).mapTo[ActorRef]
  val fSubmissionManager = (supervisor ? FindProcessingActor(SubmissionManager.name, submissionId)).mapTo[ActorRef]
  val filingPersistence: ActorRef = Await.result(fFilingPersistence, 2.seconds)
  val submissionManager: ActorRef = Await.result(fSubmissionManager, 2.seconds)

  val probe = TestProbe()

  val lines = fiCSV.split("\n")
  val timestamp = Instant.now.toEpochMilli

  "A HMDA File" must {

    // setup: create Filing object
    val filing = Filing(submissionId.period, submissionId.institutionId, NotStarted, filingRequired = false, 0L, 0L)
    probe.send(filingPersistence, CreateFiling(filing))
    probe.expectMsg(Some(filing))

    "Filing status begins as 'not started'" in {
      expectedFiling.status mustBe NotStarted
    }

    "have Filing status 'in progress' and a 'start' time after StartUpload event" in {
      probe.send(submissionManager, StartUpload)
      Thread.sleep(200)

      val filing = expectedFiling
      filing.status mustBe InProgress
      (filing.start == 0) mustBe false
      filing.end mustBe 0
    }

    "upload, parse and validate" in {
      for (line <- lines) {
        probe.send(submissionManager, AddLine(timestamp, line.toString))
      }

      probe.send(submissionManager, CompleteUpload)
      probe.send(submissionManager, GetState)
      //probe.expectMsg(Uploaded)
      Thread.sleep(5000) //TODO: can this be avoided?
      probe.send(submissionManager, GetState)
      //probe.expectMsg(ValidatedWithErrors)
    }

    "have Filing status 'completed' after signature" in {
      probe.send(submissionManager, Signed)
      Thread.sleep(200)

      //val filing = expectedFiling
      //filing.status mustBe Completed
      //(filing.end == 0) mustBe false
    }

  }

  private def expectedFiling: Filing = {
    probe.send(filingPersistence, GetFilingByPeriod(submissionId.period))
    probe.expectMsgType[Filing]
  }

}
