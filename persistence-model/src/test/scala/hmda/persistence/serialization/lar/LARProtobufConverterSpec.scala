package hmda.persistence.serialization.lar

import hmda.model.fi.lar.LarGenerators
import hmda.persistence.model.serialization.LoanApplicationRegister._
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.persistence.serialization.lar.LARProtobufConverter._

class LARProtobufConverterSpec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators {

  property("Denial must serialize to protobuf and back") {
    forAll(denialGen) { denial =>
      val protobuf = denialToProtobuf(denial).toByteArray
      denialFromProtobuf(DenialMessage.parseFrom(protobuf)) mustBe denial
    }
  }

  property("Applicant must serialize to protobuf and back") {
    forAll(applicantGen) { applicant =>
      val protobuf = applicantToProtobuf(applicant).toByteArray
      applicantFromProtobuf(ApplicantMessage.parseFrom(protobuf)) mustBe applicant
    }
  }

  property("Geography must serialize to protobuf and back") {
    forAll(geographyGen) { geography =>
      val protobuf = geographyToProtobuf(geography).toByteArray
      geographyFromProtobuf(GeographyMessage.parseFrom(protobuf)) mustBe geography
    }
  }

  property("Loan must serialize to protobuf and back") {
    forAll(loanGen) { loan =>
      val protobuf = loanToProtobuf(loan).toByteArray
      loanFromProtobuf(LoanMessage.parseFrom(protobuf)) mustBe loan
    }
  }

  property("LAR must serialize to protobuf and back") {
    forAll(larGen) { lar =>
      val protobuf = loanApplicationRegisterToProtobuf(lar).toByteArray
      loanApplicationRegisterFromProtobuf(LoanApplicationRegisterMessage.parseFrom(protobuf)) mustBe lar
    }
  }

}
