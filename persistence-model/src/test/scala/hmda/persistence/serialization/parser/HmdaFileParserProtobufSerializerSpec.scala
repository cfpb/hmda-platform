package hmda.persistence.serialization.parser

import hmda.model.fi.lar.LarGenerators
import hmda.model.fi.ts.TsGenerators
import hmda.model.parser.LarParsingError
import hmda.persistence.messages.events.processing.HmdaFileParserEvents._
import org.scalacheck.Gen
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class HmdaFileParserProtobufSerializerSpec extends PropSpec with PropertyChecks
    with MustMatchers with LarGenerators with TsGenerators {

  val serializer = new HmdaFileParserProtobufSerializer()

  def listOfStringsGen: Gen[List[String]] = Gen.listOf(Gen.alphaStr)
  def intGen: Gen[Int] = Gen.choose(0, 100000)

  property("TsParsed messages must serialize to binary and back") {
    forAll(tsGen) { ts =>
      val message = TsParsed(ts)
      val bytes = serializer.toBinary(message)
      serializer.fromBinary(bytes, serializer.TsParsedManifest) mustBe message
    }
  }
  property("TsParsedErrors messages must serialize to binary and back") {
    forAll(listOfStringsGen) { errs =>
      val message = TsParsedErrors(errs)
      val bytes = serializer.toBinary(message)
      serializer.fromBinary(bytes, serializer.TsParsedErrorsManifest) mustBe message
    }
  }
  property("LarParsed messages must serialize to binary and back") {
    forAll(larGen) { lar =>
      val message = LarParsed(lar)
      val bytes = serializer.toBinary(message)
      serializer.fromBinary(bytes, serializer.LarParsedManifest) mustBe message
    }
  }
  property("LarParsedErrors messages must serialize to binary and back") {
    forAll(intGen, listOfStringsGen) { (lineNo: Int, errs: List[String]) =>
      val message = LarParsedErrors(LarParsingError(lineNo, errs))
      val bytes = serializer.toBinary(message)
      serializer.fromBinary(bytes, serializer.LarParsedErrorsManifest) mustBe message
    }
  }
}
