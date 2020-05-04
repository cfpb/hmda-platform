package hmda.persistence.submission

import akka.actor
import akka.actor.testkit.typed.scaladsl.TestProbe
import hmda.persistence.AkkaCassandraPersistenceSpec
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.typed.{ Cluster, Join }
import hmda.messages.submission.SubmissionCommands.{ CreateSubmission, GetSubmission, ModifySubmission }
import hmda.messages.submission.SubmissionEvents.{ SubmissionCreated, SubmissionEvent, SubmissionModified, SubmissionNotExists }
import hmda.model.filing.submission.{ Created, Submission, SubmissionId, Uploaded }
import hmda.model.submission.SubmissionGenerator._
import hmda.persistence.filing.FilingPersistence
import hmda.utils.YearUtils.Period

import scala.concurrent.duration._

class SubmissionPersistenceSpec extends AkkaCassandraPersistenceSpec {
  override implicit val system      = actor.ActorSystem()
  override implicit val typedSystem = system.toTyped

  val sharding = ClusterSharding(typedSystem)
  SubmissionPersistence.startShardRegion(sharding)

  val submissionProbe = TestProbe[SubmissionEvent]("submission-probe")
  val maybeSubmissionProbe =
    TestProbe[Option[Submission]]("submission-get-probe")

  val submissionId = SubmissionId("12345", Period(2018, None), 1)

  val sampleSubmission = submissionGen
    .suchThat(s => !s.id.isEmpty)
    .suchThat(s => s.id.lei != "" && s.id.lei != "AA")
    .suchThat(s => s.status == Created)
    .sample
    .getOrElse(Submission(submissionId))

  val modified = sampleSubmission.copy(status = Uploaded)

  override def beforeAll(): Unit = {
    super.beforeAll()
    FilingPersistence.startShardRegion(ClusterSharding(system.toTyped))
  }

  "A Submission" must {
    Cluster(typedSystem).manager ! Join(Cluster(typedSystem).selfMember.address)
    "be created and read back" in {
      val submissionPersistence =
        sharding.entityRefFor(SubmissionPersistence.typeKey, s"${SubmissionPersistence.name}-${sampleSubmission.id.toString}")

      submissionPersistence ! CreateSubmission(sampleSubmission.id, submissionProbe.ref)
      submissionProbe.expectMessageType[SubmissionCreated]

      submissionPersistence ! GetSubmission(maybeSubmissionProbe.ref)
      maybeSubmissionProbe.expectMessageType[Option[Submission]]

      maybeSubmissionProbe.within(0.seconds, 5.seconds) { msg: Option[Submission] =>
        msg match {
          case Some(_) => succeed
          case None    => fail
        }
      }
    }

    "be modified and read back" in {
      val submissionPersistence =
        sharding.entityRefFor(SubmissionPersistence.typeKey, s"${SubmissionPersistence.name}-${sampleSubmission.id.toString}")

      submissionPersistence ! CreateSubmission(sampleSubmission.id, submissionProbe.ref)
      submissionProbe.expectMessageType[SubmissionCreated]

      submissionPersistence ! ModifySubmission(modified, submissionProbe.ref)
      submissionProbe.expectMessage(5.seconds, SubmissionModified(modified))
    }

    "return not exists message if trying to modify submission that doesn't exist" in {
      val submissionPersistence =
        sharding.entityRefFor(SubmissionPersistence.typeKey, s"${SubmissionPersistence.name}-X-${sampleSubmission.id.toString}")

      submissionPersistence ! ModifySubmission(modified, submissionProbe.ref)
      submissionProbe.expectMessage(SubmissionNotExists(modified.id))
    }

    "return None if it doesn't exist" in {
      val submissionPersistence =
        sharding.entityRefFor(SubmissionPersistence.typeKey, s"${SubmissionPersistence.name}-${sampleSubmission.id.toString}")

      submissionPersistence ! GetSubmission(maybeSubmissionProbe.ref)
      maybeSubmissionProbe.expectMessageType[Option[Submission]]

      maybeSubmissionProbe.within(0.seconds, 5.seconds) { msg: Option[Submission] =>
        msg match {
          case Some(_) => fail
          case None    => succeed
        }
      }
    }

  }
}