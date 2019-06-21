package hmda.api.http.codec.filing

import hmda.api.http.codec.filing.LarCodec._
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar._
import hmda.model.filing.lar._2018.LoanApplicationRegister
import io.circe.syntax._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, PropSpec}

class LarCodecSpec extends PropSpec with PropertyChecks with MustMatchers {

  property("Lar Identifier must encode/decode to/from JSON") {
    forAll(larIdentifierGen) { larId =>
      val json = larId.asJson
      json.as[LarIdentifier].getOrElse(LarIdentifier()) mustBe larId
    }
  }

  property("Loan must encode/decode to/from JSON") {
    forAll(loanGen) { loan =>
      val json = loan.asJson
      json.as[Loan].getOrElse(Loan()) mustBe loan
    }
  }

  property("LarAction must encode/decode to/from JSON") {
    forAll(larActionGen) { larAction =>
      val json = larAction.asJson
      json.as[LarAction].getOrElse(LarAction()) mustBe larAction
    }
  }

  property("Geography must encode/decode to/from JSON") {
    forAll(geographyGen) { geography =>
      val json = geography.asJson
      json.as[Geography].getOrElse(Geography()) mustBe geography
    }
  }

  property("Ethnicity must encode/decode to/from JSON") {
    forAll(ethnicityGen) { ethnicity =>
      val json = ethnicity.asJson
      json.as[Ethnicity].getOrElse(Ethnicity()) mustBe ethnicity
    }
  }

  property("Race must encode/decode to/from JSON") {
    forAll(raceGen) { race =>
      val json = race.asJson
      json.as[Race].getOrElse(Race()) mustBe race
    }
  }

  property("Sex must encode/decode to/from JSON") {
    forAll(sexGen) { sex =>
      val json = sex.asJson
      json.as[Sex].getOrElse(Sex()) mustBe sex
    }
  }

  property("Applicant must encode/decode to/from JSON") {
    forAll(applicantGen) { applicant =>
      val json = applicant.asJson
      json.as[Applicant].getOrElse(Applicant()) mustBe applicant
    }
  }

  property("Denial must encode/decode to/from JSON") {
    forAll(denialGen) { denial =>
      val json = denial.asJson
      json.as[Denial].getOrElse(Denial()) mustBe denial
    }
  }

  property("LoanDisclosure must encode/decode to/from JSON") {
    forAll(loanDisclosureGen) { loanDisclosure =>
      val json = loanDisclosure.asJson
      json.as[LoanDisclosure].getOrElse(LoanDisclosure()) mustBe loanDisclosure
    }
  }

  property("OtherNonAmortizingFeatures must encode/decode to/from JSON") {
    forAll(nonAmortizingFeaturesGen) { nonAmortizingFeatures =>
      val json = nonAmortizingFeatures.asJson
      json
        .as[NonAmortizingFeatures]
        .getOrElse(NonAmortizingFeatures()) mustBe nonAmortizingFeatures
    }
  }

  property("Property must encode/decode to/from JSON") {
    forAll(propertyGen) { property =>
      val json = property.asJson
      json.as[Property].getOrElse(Property()) mustBe property
    }
  }

  property("AutomatedUnderwritingSystem must encode/decode to/from JSON") {
    forAll(automatedUnderwritingSystemGen) { aus =>
      val json = aus.asJson
      json
        .as[AutomatedUnderwritingSystem]
        .getOrElse(AutomatedUnderwritingSystem()) mustBe aus
    }
  }

  property("AutomatedUnderwritingSystemResult must encode/decode to/from JSON") {
    forAll(automatedUnderwritingSystemResultGen) { ausResult =>
      val json = ausResult.asJson
      json
        .as[AutomatedUnderwritingSystemResult]
        .getOrElse(AutomatedUnderwritingSystemResult()) mustBe ausResult
    }
  }

  property("LoanApplicationRegister must encode/decode to/from JSON") {
    forAll(larGen) { lar =>
      val json = lar.asJson
      json
        .as[LoanApplicationRegister]
        .getOrElse(LoanApplicationRegister()) mustBe lar
    }
  }

}
