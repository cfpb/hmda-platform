package hmda.persistence.institutions

import akka.actor.ActorSystem
import akka.testkit.{ EventFilter, TestProbe }
import com.typesafe.config.ConfigFactory
import hmda.actor.test.ActorSpec
import hmda.model.fi._
import hmda.persistence.CommonMessages.GetState
import hmda.persistence.demo.DemoData
import hmda.persistence.institutions.SubmissionPersistence.{ CreateSubmission, GetSubmissionById, UpdateSubmissionStatus, _ }
import hmda.persistence.processing.TestConfigOverride

class SubmissionPersistenceSpec extends ActorSpec {

  val config = ConfigFactory.load()
  override implicit lazy val system =
    ActorSystem(
      "test-system",
      ConfigFactory.parseString(
        TestConfigOverride.config
      )
    )

  val submissionsActor = createSubmissions("0", "2017", system)

  val probe = TestProbe()

  "Submissions" must {
    "be created and read back" in {
      val submissions = DemoData.testSubmissions
      for (submission <- submissions) {
        probe.send(submissionsActor, CreateSubmission)
      }
      probe.send(submissionsActor, GetState)
      probe.expectMsg(DemoData.testSubmissions.reverse)
    }

    "be able to modify their status" in {
      val newStatus = Uploaded
      val id = SubmissionId("0", "2017", 1)
      probe.send(submissionsActor, UpdateSubmissionStatus(id, newStatus))
      probe.send(submissionsActor, GetSubmissionById(id))
      probe.expectMsg(Submission(id, Uploaded))
    }
  }

  "Error logging" must {
    "warn when updating nonexistent submission" in {
      val id = SubmissionId("0", "2017", 777)
      val msg = s"Submission does not exist. Could not update submission with id $id"
      EventFilter.warning(message = msg, occurrences = 1) intercept {
        probe.send(submissionsActor, UpdateSubmissionStatus(id, Created))
      }
    }
  }

}
