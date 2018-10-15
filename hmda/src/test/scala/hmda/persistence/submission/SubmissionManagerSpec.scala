package hmda.persistence.submission

import akka.actor
import akka.actor.testkit.typed.scaladsl.TestProbe
import hmda.persistence.AkkaCassandraPersistenceSpec
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.typed.{Cluster, Join}
import hmda.messages.submission.SubmissionManagerCommands._
import hmda.model.filing.submission._

class SubmissionManagerSpec extends AkkaCassandraPersistenceSpec {
  override implicit val system = actor.ActorSystem()
  override implicit val typedSystem = system.toTyped

  val sharding = ClusterSharding(typedSystem)

  Cluster(typedSystem).manager ! Join(Cluster(typedSystem).selfMember.address)

  val submissionId = SubmissionId("12345", "2018", 1)

  val submissionManagerProbe =
    TestProbe[SubmissionStatus]("submission-manager-probe")

  override def beforeAll(): Unit = {
    super.beforeAll()
    SubmissionManager.startShardRegion(sharding)
  }

  "Submission Manager" must {
    "have status Created when starting" in {
      val submissionManager = sharding.entityRefFor(
        SubmissionManager.typeKey,
        s"${SubmissionManager.name}-${submissionId.toString}")
      submissionManager ! GetSubmissionStatus(submissionManagerProbe.ref)
      submissionManagerProbe.expectMessage(Created)
    }
    "start parsing" in {
      val submissionManager = sharding.entityRefFor(
        SubmissionManager.typeKey,
        s"${SubmissionManager.name}-${submissionId.toString}")
      submissionManager ! StartParsing(submissionId)
      submissionManager ! GetSubmissionStatus(submissionManagerProbe.ref)
      submissionManagerProbe.expectMessage(Parsing)
    }
    "complete parsing with errors" in {
      val submissionManager = sharding.entityRefFor(
        SubmissionManager.typeKey,
        s"${SubmissionManager.name}-${submissionId.toString}")
      submissionManager ! CompleteParsingWithErrors(submissionId)
      submissionManager ! GetSubmissionStatus(submissionManagerProbe.ref)
      submissionManagerProbe.expectMessage(ParsedWithErrors)
    }
    "complete parsing" in {
      val submissionManager = sharding.entityRefFor(
        SubmissionManager.typeKey,
        s"${SubmissionManager.name}-${submissionId.toString}")
      submissionManager ! CompleteParsing(submissionId)
      submissionManager ! GetSubmissionStatus(submissionManagerProbe.ref)
      submissionManagerProbe.expectMessage(Parsed)
    }
    "start validating" in {
      val submissionManager = sharding.entityRefFor(
        SubmissionManager.typeKey,
        s"${SubmissionManager.name}-${submissionId.toString}")
      submissionManager ! StartSyntacticalValidity(submissionId)
      submissionManager ! GetSubmissionStatus(submissionManagerProbe.ref)
      submissionManagerProbe.expectMessage(Validating)
    }
    "complete syntactical/validity with errors" in {
      val submissionManager = sharding.entityRefFor(
        SubmissionManager.typeKey,
        s"${SubmissionManager.name}-${submissionId.toString}")
      submissionManager ! CompleteSyntacticalValidityWithErrors(submissionId)
      submissionManager ! GetSubmissionStatus(submissionManagerProbe.ref)
      submissionManagerProbe.expectMessage(SyntacticalOrValidityErrors)
    }
    "complete syntactical/validity" in {
      val submissionManager = sharding.entityRefFor(
        SubmissionManager.typeKey,
        s"${SubmissionManager.name}-${submissionId.toString}")
      submissionManager ! CompleteSyntacticalValidity(submissionId)
      submissionManager ! GetSubmissionStatus(submissionManagerProbe.ref)
      submissionManagerProbe.expectMessage(SyntacticalOrValidity)
    }
    "start quality" in {
      val submissionManager = sharding.entityRefFor(
        SubmissionManager.typeKey,
        s"${SubmissionManager.name}-${submissionId.toString}")
      submissionManager ! CompleteQuality(submissionId)
      submissionManager ! GetSubmissionStatus(submissionManagerProbe.ref)
      submissionManagerProbe.expectMessage(Quality)

    }
    "complete quality with errors" in {
      val submissionManager = sharding.entityRefFor(
        SubmissionManager.typeKey,
        s"${SubmissionManager.name}-${submissionId.toString}")
      submissionManager ! CompleteQualityWithErrors(submissionId)
      submissionManager ! GetSubmissionStatus(submissionManagerProbe.ref)
      submissionManagerProbe.expectMessage(QualityErrors)
    }
    "start macro" in {
      val submissionManager = sharding.entityRefFor(
        SubmissionManager.typeKey,
        s"${SubmissionManager.name}-${submissionId.toString}")
      submissionManager ! CompleteMacro(submissionId)
      submissionManager ! GetSubmissionStatus(submissionManagerProbe.ref)
      submissionManagerProbe.expectMessage(Macro)
    }
    "complete macro with errors" in {
      val submissionManager = sharding.entityRefFor(
        SubmissionManager.typeKey,
        s"${SubmissionManager.name}-${submissionId.toString}")
      submissionManager ! CompleteMacroWithErrors(submissionId)
      submissionManager ! GetSubmissionStatus(submissionManagerProbe.ref)
      submissionManagerProbe.expectMessage(MacroErrors)
    }
    "be verified" in {
      val submissionManager = sharding.entityRefFor(
        SubmissionManager.typeKey,
        s"${SubmissionManager.name}-${submissionId.toString}")
      submissionManager ! Verify(submissionId)
      submissionManager ! GetSubmissionStatus(submissionManagerProbe.ref)
      submissionManagerProbe.expectMessage(Verified)
    }
    "be signed" in {
      val submissionManager = sharding.entityRefFor(
        SubmissionManager.typeKey,
        s"${SubmissionManager.name}-${submissionId.toString}")
      submissionManager ! Sign(submissionId)
      submissionManager ! GetSubmissionStatus(submissionManagerProbe.ref)
      submissionManagerProbe.expectMessage(Signed)
    }
    "be failed" in {
      val submissionManager = sharding.entityRefFor(
        SubmissionManager.typeKey,
        s"${SubmissionManager.name}-${submissionId.toString}")
      submissionManager ! Fail(submissionId)
      submissionManager ! GetSubmissionStatus(submissionManagerProbe.ref)
      submissionManagerProbe.expectMessage(Failed)
    }

  }
}
