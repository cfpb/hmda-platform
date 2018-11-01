package hmda.serialization.submission

import org.scalatest.{MustMatchers, PropSpec}
import org.scalatest.prop.PropertyChecks
import hmda.model.submission.SubmissionGenerator._
import SubmissionProcessingCommandsProtobufConverter._
import hmda.messages.submission.SubmissionProcessingCommands._
import hmda.persistence.serialization.submission.processing.commands._
import org.scalacheck.Gen

class SubmissionProcessingCommandsProtobufConverterSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers {

  property("Start Upload must serialize to protobuf and back") {
    forAll(submissionIdGen) { submissionId =>
      val cmd = StartUpload(submissionId)
      val protobuf = startUploadToProtobuf(cmd).toByteArray
      startUploadFromProtobuf(StartUploadMessage.parseFrom(protobuf)) mustBe cmd
    }
  }

  property("Complete Upload must serialize to protobuf and back") {
    forAll(submissionIdGen) { submissionId =>
      val cmd = CompleteUpload(submissionId)
      val protobuf = completeUploadToProtobuf(cmd).toByteArray
      completeUploadFromProtobuf(CompleteUploadMessage.parseFrom(protobuf)) mustBe cmd
    }
  }

  property("Start Parsing must serialize to protobuf and back") {
    forAll(submissionIdGen) { submissionId =>
      val cmd = StartParsing(submissionId)
      val protobuf = startParsingToProtobuf(cmd).toByteArray
      startParsingFromProtobuf(StartParsingMessage.parseFrom(protobuf)) mustBe cmd
    }
  }

  property("Persist Parser Errors must serialize to protobuf and back") {
    val rowNumberGen = Gen.choose(0, Int.MaxValue)
    val errorListGen = Gen.listOf(Gen.alphaStr)
    implicit def persistParsedErrorGen: Gen[PersistHmdaRowParsedError] = {
      for {
        rowNumber <- rowNumberGen
        errors <- errorListGen
      } yield PersistHmdaRowParsedError(rowNumber, errors)
    }

    forAll(persistParsedErrorGen) { cmd =>
      val protobuf = persistHmdaRowParsedErrorToProtobuf(cmd).toByteArray
      persisteHmdaRowParsedErrorFromProtobuf(
        PersistHmdaRowParsedErrorMessage.parseFrom(protobuf)) mustBe cmd
    }
  }

  property("Get Parser Error Count must serialize to protobuf and back") {
    pending
  }

  property("Complete Parsing must serialize to protobuf and back") {
    forAll(submissionIdGen) { submissionId =>
      val cmd = CompleteParsing(submissionId)
      val protobuf = completeParsingToProtobuf(cmd).toByteArray
      completeParsingFromProtobuf(CompleteParsingMessage.parseFrom(protobuf)) mustBe cmd
    }
  }

}
