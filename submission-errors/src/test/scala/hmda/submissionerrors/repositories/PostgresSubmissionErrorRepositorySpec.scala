package hmda.submissionerrors.repositories

import akka.actor.ActorSystem
import akka.testkit.TestKit
import hmda.model.filing.submission.SubmissionId
import hmda.utils.EmbeddedPostgres
import hmda.utils.YearUtils.Period
import monix.execution.Scheduler
import org.scalatest.{ FlatSpecLike, Matchers }

class PostgresSubmissionErrorRepositorySpec
  extends TestKit(ActorSystem("submission-error-repo-spec"))
    with FlatSpecLike
    with Matchers
    with EmbeddedPostgres {
  implicit val monixScheduler: Scheduler = Scheduler(system.dispatcher)

  override def bootstrapSqlFile: String = "bootstrap.sql"

  "PostgresSubmissionErrorRepository" should "persist error data into the repository" in {
    // Note: dbConfig comes from EmbeddedPostgres
    val dbConfig     = PostgresSubmissionErrorRepository.config(dbHoconpath)
    val repo         = PostgresSubmissionErrorRepository.make(dbConfig, "triggered_quality_edits")
    val submissionId = SubmissionId("EXAMPLE-LEI", Period(2018, None), sequenceNumber = 1)

    repo.submissionPresent(submissionId).runSyncUnsafe() shouldBe false

    repo
      .add(submissionId, submissionStatus = 10, List(AddSubmissionError("EXAMPLE", Vector("ULI1:ActionTaken1:ActionTakenDate"))))
      .runSyncUnsafe()

    repo.submissionPresent(submissionId).runSyncUnsafe() shouldBe true
  }
}