package hmda.persistence.serialization.parser

import hmda.model.fi.lar.LarGenerators
import hmda.model.fi.ts.TsGenerators
import hmda.model.parser.LarParsingError
import hmda.persistence.messages.events.processing.HmdaFileParserEvents.{ LarParsed, LarParsedErrors, TsParsed, TsParsedErrors }
import hmda.persistence.model.serialization.HmdaFileParserEvents._
import hmda.persistence.serialization.parser.HmdaFileParserProtobufConverter._
import org.scalacheck.Gen
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class HmdaFileParserProtobufConverterSpec extends PropSpec with PropertyChecks with MustMatchers
    with TsGenerators with LarGenerators {

  def listOfStringsGen: Gen[List[String]] = Gen.listOf(Gen.alphaStr)
  def intGen: Gen[Int] = Gen.choose(0, 100000)

  property("TsParsed must convert to protobuf and back") {
    forAll(tsGen) { ts =>
      val message = TsParsed(ts)
      val protobuf = tsParsedToProtobuf(message).toByteArray
      tsParsedFromProtobuf(TsParsedMessage.parseFrom(protobuf)) mustBe message
    }
  }

  property("TsParsedErrors must convert to protobuf and back") {
    forAll(listOfStringsGen) { errs =>
      val message = TsParsedErrors(errs)
      val protobuf = tsParsedErrorsToProtobuf(message).toByteArray
      tsParsedErrorsFromProtobuf(TsParsedErrorsMessage.parseFrom(protobuf)) mustBe message
    }
  }

  property("LarParsed must convert to protobuf and back") {
    forAll(larGen) { lar =>
      val message = LarParsed(lar)
      val protobuf = larParsedToProtobuf(message).toByteArray
      larParsedFromProtobuf(LarParsedMessage.parseFrom(protobuf)) mustBe message
    }
  }

  property("LarParsingError must convert to protobuf and back") {
    forAll(intGen, listOfStringsGen) { (lineNo, errs) =>
      val message = LarParsingError(lineNo, errs)
      val protobuf = larParsingErrorToProtobuf(message).toByteArray
      larParsingErrorFromProtobuf(LarParsingErrorMessage.parseFrom(protobuf)) mustBe message
    }
  }

  property("LarParsedErrors must convert to protobuf and back") {
    forAll(intGen, listOfStringsGen) { (lineNo, errs) =>
      val message = LarParsedErrors(LarParsingError(lineNo, errs))
      val protobuf = larParsedErrorsToProtobuf(message).toByteArray
      larParsedErrorsFromProtobuf(LarParsedErrorsMessage.parseFrom(protobuf)) mustBe message
    }
  }
}
