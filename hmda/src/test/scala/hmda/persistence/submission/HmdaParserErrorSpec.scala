package hmda.persistence.submission

import akka.actor
import akka.actor.testkit.typed.scaladsl.TestProbe
import hmda.persistence.AkkaCassandraPersistenceSpec
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.typed.{ Cluster, Join }
import hmda.messages.submission.SubmissionProcessingCommands.{ GetParsedWithErrorCount, GetParsingErrors, PersistHmdaRowParsedError }
import hmda.messages.submission.SubmissionProcessingEvents.{ HmdaRowParsedCount, HmdaRowParsedError, SubmissionProcessingEvent }
import hmda.model.filing.submission.SubmissionId
import hmda.model.processing.state.HmdaParserErrorState
import hmda.parser.filing.lar.LarParserErrorModel.{ InvalidLoanTerm, InvalidOccupancy }
import hmda.parser.filing.ts.TsParserErrorModel.InvalidTsId
import hmda.messages.submission.SubmissionProcessingCommands._
import hmda.utils.YearUtils.Period

import scala.util.Random

class HmdaParserErrorSpec extends AkkaCassandraPersistenceSpec {
  override implicit val system      = actor.ActorSystem()
  override implicit val typedSystem = system.toTyped

  val sharding = ClusterSharding(typedSystem)
  SubmissionManager.startShardRegion(sharding)
  SubmissionPersistence.startShardRegion(sharding)
  HmdaParserError.startShardRegion(sharding)

  val submissionId = SubmissionId(Random.nextInt(12345).toString, Period(2018, None), 1)

  val errorsProbe = TestProbe[SubmissionProcessingEvent]("processing-event")
  val stateProbe  = TestProbe[HmdaParserErrorState]("parser-errors")

  "Parser errors" must {
    Cluster(typedSystem).manager ! Join(Cluster(typedSystem).selfMember.address)
    "be persisted and retrieved back" in {
      val hmdaParserError = sharding.entityRefFor(HmdaParserError.typeKey, s"${HmdaParserError.name}-${submissionId.toString}")
      val e1              = List(InvalidTsId("a"))
      val e2              = List(InvalidLoanTerm("a"), InvalidOccupancy("a"))
      hmdaParserError ! PersistHmdaRowParsedError(1, "testULI", e1.map(x => FieldParserError(x.fieldName, x.inputValue)), None)
      hmdaParserError ! PersistHmdaRowParsedError(2, "testULI", e2.map(x => FieldParserError(x.fieldName, x.inputValue)), None)
      hmdaParserError ! GetParsedWithErrorCount(errorsProbe.ref)
      errorsProbe.expectMessage(HmdaRowParsedCount(2))

      hmdaParserError ! GetParsingErrors(1, stateProbe.ref)
      stateProbe.expectMessage(
        HmdaParserErrorState(
          List(HmdaRowParsedError(1, "testULI", e1.map(x => FieldParserError(x.fieldName, x.inputValue)))),
          List(HmdaRowParsedError(2, "testULI", e2.map(x => FieldParserError(x.fieldName, x.inputValue)))),
          2
        )
      )
    }
  }
}