package hmda.persistence.filing

import akka.actor
import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.typed.{ Cluster, Join }
import hmda.messages.filing.FilingCommands._
import hmda.messages.filing.FilingEvents.FilingCreated
import hmda.model.filing.{ Filing, FilingDetails, FilingId }
import hmda.persistence.AkkaCassandraPersistenceSpec
import hmda.model.filing.FilingGenerator._
import hmda.model.filing.submission._
import hmda.model.submission.SubmissionGenerator._
import hmda.persistence.institution.InstitutionPersistence
import hmda.utils.YearUtils.Period

class FilingPersistenceSpec extends AkkaCassandraPersistenceSpec {
  override implicit val system      = actor.ActorSystem()
  override implicit val typedSystem = system.toTyped

  val sharding = ClusterSharding(typedSystem)
  FilingPersistence.startShardRegion(sharding)

  val filingCreatedProbe = TestProbe[FilingCreated]("filing-created-probe")
  val maybeFilingProbe   = TestProbe[Option[Filing]]("maybe-filing-probe")
  val filingDetailsProbe =
    TestProbe[Option[FilingDetails]]("filing-details-probe")
  val maybeSubmissionProbe =
    TestProbe[Option[Submission]]("maybe-submission-probe")
  val submissionProbe  = TestProbe[Submission]("submission-probe")
  val submissionsProbe = TestProbe[List[Submission]](name = "submissions-probe")

  val sampleFiling = filingGen
    .suchThat(_.lei != "")
    .suchThat(_.period != "")
    .sample
    .getOrElse(Filing())

  val sampleSubmission = submissionGen
    .suchThat(s => !s.id.isEmpty)
    .suchThat(s => s.id.lei != "" && s.id.lei != "AA")
    .suchThat(s => s.status == SubmissionStatus.valueOf(QualityErrors.code))
    .sample
    .getOrElse(Submission(SubmissionId("12345", Period(2018, None), 1)))

  val modified = sampleSubmission.copy(status = Uploaded)

  override def beforeAll(): Unit = {
    super.beforeAll()
    InstitutionPersistence.startShardRegion(ClusterSharding(system.toTyped))
  }

  "Filings" must {
    Cluster(typedSystem).manager ! Join(Cluster(typedSystem).selfMember.address)
    "be created and read back" in {
      val filingPersistence = sharding.entityRefFor(
        FilingPersistence.typeKey,
        s"${FilingPersistence.name}-${FilingId(sampleFiling.lei, sampleFiling.period).toString}"
      )

      filingPersistence ! GetFiling(maybeFilingProbe.ref)
      maybeFilingProbe.expectMessage(None)

      filingPersistence ! GetFilingDetails(filingDetailsProbe.ref)
      filingDetailsProbe.expectMessage(None)

      filingPersistence ! CreateFiling(sampleFiling, filingCreatedProbe.ref)
      filingCreatedProbe.expectMessage(FilingCreated(sampleFiling))

      filingPersistence ! GetFiling(maybeFilingProbe.ref)
      maybeFilingProbe.expectMessage(Some(sampleFiling))
    }
    "create submissions, modify some and read them back" in {
      val filingPersistence = sharding.entityRefFor(
        FilingPersistence.typeKey,
        s"${FilingPersistence.name}-${FilingId(sampleFiling.lei, sampleFiling.period).toString}"
      )

      filingPersistence ! CreateFiling(sampleFiling, filingCreatedProbe.ref)
      filingCreatedProbe.expectMessage(FilingCreated(sampleFiling))

      filingPersistence ! GetLatestSubmission(maybeSubmissionProbe.ref)
      maybeSubmissionProbe.expectMessage(None)

      filingPersistence ! AddSubmission(sampleSubmission, Some(submissionProbe.ref))
      submissionProbe.expectMessage(sampleSubmission)

      filingPersistence ! GetLatestSubmission(maybeSubmissionProbe.ref)
      maybeSubmissionProbe.expectMessage(Some(sampleSubmission))

      val sampleSubmission2 =
        sampleSubmission.copy(id = sampleSubmission.id.copy(lei = "AAA"))
      filingPersistence ! AddSubmission(sampleSubmission2, Some(submissionProbe.ref))
      submissionProbe.expectMessage(sampleSubmission2)

      filingPersistence ! GetSubmissions(submissionsProbe.ref)
      submissionsProbe.expectMessage(List(sampleSubmission2, sampleSubmission))

      filingPersistence ! GetLatestSubmission(maybeSubmissionProbe.ref)
      maybeSubmissionProbe.expectMessage(Some(sampleSubmission2))

      val updated =
        sampleSubmission2.copy(status = SubmissionStatus.valueOf(Verified.code))
      filingPersistence ! UpdateSubmission(updated, Some(submissionProbe.ref))
    }
  }
}