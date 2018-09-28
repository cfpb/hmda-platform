package hmda.persistence.filing

import akka.actor
import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.actor.typed.scaladsl.adapter._
import hmda.messages.filing.FilingCommands.{AddSubmission, GetLatestSubmission}
import hmda.messages.filing.FilingEvents.FilingEvent
import hmda.model.filing.Filing
import hmda.persistence.AkkaCassandraPersistenceSpec
import hmda.model.filing.FilingGenerator._
import hmda.model.filing.submission.{Submission, SubmissionId}
import hmda.model.submission.SubmissionGenerator._

class FilingPersistenceSpec extends AkkaCassandraPersistenceSpec {
  override implicit val system = actor.ActorSystem()
  override implicit val typedSystem = system.toTyped

  val filingProbe = TestProbe[FilingEvent]("filing-event-probe")
  val maybeSubmissionProbe =
    TestProbe[Option[Submission]]("maybe-submission-probe")
  val submissionProbe = TestProbe[Submission]("submission-probe")

  val sampleFiling = filingGen
    .suchThat(_.lei != "")
    .suchThat(_.period != "")
    .sample
    .getOrElse(Filing())

  val sampleSubmission = submissionGen
    .suchThat(s => !s.id.isEmpty)
    .suchThat(s => s.id.lei != "" && s.id.lei != "AA")
    .sample
    .getOrElse(Submission(SubmissionId("12345", "2018", 1)))

  "A Filing" must {
    "be created and read back" in {
      val filingPersistence = system.spawn(
        FilingPersistence.behavior(sampleFiling.lei, sampleFiling.period),
        actorName)
      filingPersistence ! AddSubmission(sampleSubmission, submissionProbe.ref)
      submissionProbe.expectMessage(sampleSubmission)

      filingPersistence ! GetLatestSubmission(maybeSubmissionProbe.ref)
      maybeSubmissionProbe.expectMessage(Some(sampleSubmission))
    }
  }
}
