package hmda.persistence.submission

import akka.actor
import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.typed.{ Cluster, Join }
import akka.http.scaladsl.testkit.RouteTestTimeout
import hmda.messages.filing.FilingCommands.GetLatestSubmission
import hmda.messages.submission.SubmissionCommands.{ CreateSubmission, GetSubmission }
import hmda.messages.submission.SubmissionEvents.{ SubmissionCreated, SubmissionEvent }
import hmda.messages.submission.SubmissionManagerCommands._
import hmda.model.filing.FilingGenerator.filingGen
import hmda.model.filing.{ Filing, FilingId }
import hmda.model.filing.submission._
import hmda.model.submission.SubmissionGenerator.submissionGen
import hmda.persistence.AkkaCassandraPersistenceSpec
import hmda.persistence.filing.FilingPersistence
import hmda.utils.YearUtils.Period
import org.scalatest.{ BeforeAndAfterAll, MustMatchers }
import akka.testkit._

import scala.concurrent.duration._

class SubmissionManagerSpec extends AkkaCassandraPersistenceSpec with MustMatchers with BeforeAndAfterAll {
  implicit val system      = actor.ActorSystem()
  implicit val typedSystem = system.toTyped

  val duration = 10.seconds

  implicit val routeTimeout = RouteTestTimeout(duration.dilated)

  val sharding = ClusterSharding(typedSystem)

  Cluster(typedSystem).manager ! Join(Cluster(typedSystem).selfMember.address)

  val submissionId = SubmissionId("12345", Period(2018, None), 1)

  val maybeFilingProbe = TestProbe[Option[Filing]]("maybe-filing-probe")
  val submissionProbe  = TestProbe[SubmissionEvent]("submission-probe")
  val maybeSubmissionProbe =
    TestProbe[Option[Submission]]("submission-get-probe")

  val sampleFiling = filingGen
    .suchThat(_.lei != "")
    .suchThat(_.period != "")
    .sample
    .getOrElse(Filing())

  val sampleSubmission = submissionGen
    .suchThat(s => !s.id.isEmpty)
    .suchThat(s => s.id.lei != "" && s.id.lei != "AA")
    .suchThat(s => s.status == Created)
    .sample
    .getOrElse(Submission(submissionId))

  val uploaded = sampleSubmission.copy(status = Uploaded)

  override def beforeAll(): Unit = {
    super.beforeAll()
    SubmissionManager.startShardRegion(sharding)
    SubmissionPersistence.startShardRegion(ClusterSharding(system.toTyped))
    FilingPersistence.startShardRegion(ClusterSharding(system.toTyped))
  }

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  "Submission Manager" must {
    "Update submission and filing" in {
      val submissionManager = sharding.entityRefFor(SubmissionManager.typeKey, s"${SubmissionManager.name}-${sampleSubmission.id.toString}")

      val submissionPersistence =
        sharding.entityRefFor(SubmissionPersistence.typeKey, s"${SubmissionPersistence.name}-${sampleSubmission.id.toString}")

      val filingPersistence = sharding.entityRefFor(
        FilingPersistence.typeKey,
        s"${FilingPersistence.name}-${FilingId(sampleSubmission.id.lei, sampleSubmission.id.period.toString).toString}"
      )

      submissionPersistence ! CreateSubmission(sampleSubmission.id, submissionProbe.ref)
      submissionProbe.expectMessageType[SubmissionCreated]

      submissionPersistence ! GetSubmission(maybeSubmissionProbe.ref)
      maybeSubmissionProbe.expectMessageType[Option[Submission]]

      maybeSubmissionProbe.within(0.seconds, 5.seconds) { msg: Option[Submission] =>
        msg match {
          case Some(s) => s mustBe sampleSubmission
          case None    => fail
        }
      }

      filingPersistence ! GetLatestSubmission(maybeSubmissionProbe.ref)
      maybeSubmissionProbe.within(0.seconds, 5.seconds) { msg: Option[Submission] =>
        msg match {
          case Some(s) =>
            s.id mustBe sampleSubmission.id
            s.status mustBe sampleSubmission.status
          case None => fail
        }
      }

      submissionManager ! UpdateSubmissionStatus(uploaded)
      submissionPersistence ! GetSubmission(maybeSubmissionProbe.ref)
      maybeSubmissionProbe.expectMessageType[Option[Submission]]

      maybeSubmissionProbe.within(0.seconds, 5.seconds) { msg: Option[Submission] =>
        msg match {
          case Some(s) => s mustBe uploaded
          case None    => fail
        }
      }

      filingPersistence ! GetLatestSubmission(maybeSubmissionProbe.ref)
      maybeSubmissionProbe.within(0.seconds, 5.seconds) { msg: Option[Submission] =>
        msg match {
          case Some(s) =>
            s.id mustBe sampleSubmission.id
            s.status mustBe uploaded.status
          case None => fail
        }
      }
    }
  }
}