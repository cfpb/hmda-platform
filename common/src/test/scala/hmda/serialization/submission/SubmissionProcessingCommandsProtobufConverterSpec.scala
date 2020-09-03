package hmda.serialization.submission

import akka.actor.ActorSystem
import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.actor.typed.ActorRefResolver
import akka.actor.typed.scaladsl.adapter._
import hmda.generators.CommonGenerators.emailGen
import hmda.messages.submission.SubmissionProcessingCommands._
import hmda.messages.submission.SubmissionProcessingEvents.{SubmissionProcessingEvent, SubmissionSignedEvent}
import hmda.model.processing.state.HmdaParserErrorState
import hmda.model.submission.SubmissionGenerator._
import hmda.persistence.serialization.submission.processing.commands._
import hmda.serialization.submission.HmdaParserErrorStateGenerator._
import hmda.serialization.submission.SubmissionProcessingCommandsProtobufConverter._
import hmda.serialization.validation.ValidationErrorGenerator._
import org.scalacheck.Gen
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class SubmissionProcessingCommandsProtobufConverterSpec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  implicit val system                    = ActorSystem()
  implicit val typedSystem               = system.toTyped
  val actorRefResolver: ActorRefResolver = ActorRefResolver(typedSystem)

  property("Start Upload must serialize to protobuf and back") {
    forAll(submissionIdGen) { submissionId =>
      val cmd      = StartUpload(submissionId)
      val protobuf = startUploadToProtobuf(cmd).toByteArray
      startUploadFromProtobuf(StartUploadMessage.parseFrom(protobuf)) mustBe cmd
    }
  }

  property("Complete Upload must serialize to protobuf and back") {
    forAll(submissionIdGen) { submissionId =>
      val cmd      = CompleteUpload(submissionId)
      val protobuf = completeUploadToProtobuf(cmd).toByteArray
      completeUploadFromProtobuf(CompleteUploadMessage.parseFrom(protobuf)) mustBe cmd
    }
  }

  property("Start Parsing must serialize to protobuf and back") {
    forAll(submissionIdGen) { submissionId =>
      val cmd      = StartParsing(submissionId)
      val protobuf = startParsingToProtobuf(cmd).toByteArray
      startParsingFromProtobuf(StartParsingMessage.parseFrom(protobuf)) mustBe cmd
    }
  }

  property("Persist Parser Errors must serialize to protobuf and back") {
    val rowNumberGen    = Gen.choose(0, Int.MaxValue)
    val estimatedULIGen = Gen.alphaStr
    val errorListGen    = Gen.listOf(FieldParserErrorGen)
    implicit def persistParsedErrorGen: Gen[PersistHmdaRowParsedError] =
      for {
        rowNumber    <- rowNumberGen
        estimatedULI <- estimatedULIGen
        errors       <- errorListGen
      } yield PersistHmdaRowParsedError(rowNumber, estimatedULI, errors, None)

    forAll(persistParsedErrorGen) { cmd =>
      val protobuf =
        persistHmdaRowParsedErrorToProtobuf(cmd, actorRefResolver).toByteArray
      persistHmdaRowParsedErrorFromProtobuf(PersistHmdaRowParsedErrorMessage.parseFrom(protobuf), actorRefResolver) mustBe cmd
    }
  }

  property("Get Parser Error Count must serialize to protobuf and back") {
    val probe    = TestProbe[SubmissionProcessingEvent]
    val actorRef = probe.ref
    val resolver = ActorRefResolver(typedSystem)
    val cmd      = GetParsedWithErrorCount(actorRef)
    val protobuf = getParsedWithErrorCountToProtobuf(cmd, resolver).toByteArray
    getParsedWithErrorCountFromProtobuf(GetParsedWithErrorCountMessage.parseFrom(protobuf), resolver) mustBe cmd
  }

  property("Get Parsing Errors must serialize to protobuf and back") {
    val probe    = TestProbe[HmdaParserErrorState]
    val actorRef = probe.ref
    val resolver = ActorRefResolver(typedSystem)
    val cmd      = GetParsingErrors(2, actorRef)
    val protobuf = getParsingErrorsToProtobuf(cmd, resolver).toByteArray
    getParsingErrorsFromProtobuf(GetParsingErrorsMessage.parseFrom(protobuf), resolver) mustBe cmd
  }

  property("Complete Parsing must serialize to protobuf and back") {
    forAll(submissionIdGen) { submissionId =>
      val cmd      = CompleteParsing(submissionId)
      val protobuf = completeParsingToProtobuf(cmd).toByteArray
      completeParsingFromProtobuf(CompleteParsingMessage.parseFrom(protobuf)) mustBe cmd
    }
  }

  property("Complete Parsing With Errors must serialize to protobuf and back") {
    forAll(submissionIdGen) { submissionId =>
      val cmd      = CompleteParsingWithErrors(submissionId)
      val protobuf = completeParsingWithErrorsToProtobuf(cmd).toByteArray
      completeParsingWithErrorsFromProtobuf(CompleteParsingWithErrorsMessage.parseFrom(protobuf)) mustBe cmd
    }
  }

  property("Start Syntactical and Validity must serialize to protobuf and back") {
    forAll(submissionIdGen) { submissionId =>
      val cmd      = StartSyntacticalValidity(submissionId)
      val protobuf = startSyntacticalValidityToProtobuf(cmd).toByteArray
      startSyntacticalValidityFromProtobuf(StartSyntacticalValidityMessage.parseFrom(protobuf)) mustBe cmd
    }
  }

  property("PersistHmdaRowValidatedError must serialize to protobuf and back") {
    forAll(submissionIdGen, validationErrorGen) { (submissionId, validationError) =>
      val persistHmdaRowValidatedError =
        PersistHmdaRowValidatedError(submissionId, 1, List(validationError), None)
      val protobuf =
        persistHmdaRowValidatedErrorToProtobuf(persistHmdaRowValidatedError, actorRefResolver).toByteArray
      persistHmdaRowValidatedErrorFromProtobuf(PersistHmdaRowValidatedErrorMessage.parseFrom(protobuf), actorRefResolver) mustBe persistHmdaRowValidatedError
    }
  }

  property("SignSubmission must serialize to protobuf and back") {
    forAll(submissionIdGen, emailGen, Gen.asciiStr) { (submissionId, email, username) =>
      val probe    = TestProbe[SubmissionSignedEvent]
      val actorRef = probe.ref
      val resolver = ActorRefResolver(typedSystem)
      val cmd      = SignSubmission(submissionId, actorRef, email, username)
      val protobuf = signSubmissionToProtobuf(cmd, actorRefResolver).toByteArray
      signSubmissionFromProtobuf(SignSubmissionMessage.parseFrom(protobuf), actorRefResolver) mustBe cmd
    }
  }


}