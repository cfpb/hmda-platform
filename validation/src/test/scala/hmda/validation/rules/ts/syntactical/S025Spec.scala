package hmda.validation.rules.ts.syntactical

import hmda.model.fi.ts.{ Respondent, TransmittalSheet }
import hmda.model.institution.Agency.CFPB
import hmda.model.institution.ExternalIdType.{ FdicCertNo, FederalTaxId, RssdId }
import hmda.model.institution.InstitutionType.Bank
import hmda.model.institution.{ ExternalId, Institution }
import hmda.parser.fi.ts.TsGenerators
import hmda.validation.dsl.{ Failure, Success }
import org.scalacheck.Gen
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

import scala.concurrent.ExecutionContext

/**
 * Created by keelerh on 7/22/16.
 */
class S025Spec extends PropSpec with PropertyChecks with MustMatchers with TsGenerators {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  //Generate all Transmittal Sheets with hardcoded Respondent
  override implicit def tsGen: Gen[TransmittalSheet] = {
    for {
      timeStamp <- timeGen
      activityYear <- activityYearGen
      taxId <- taxIdGen
      parent <- parentGen
      contact <- contactGen
    } yield TransmittalSheet(
      1,
      9, //CFPB
      timeStamp,
      activityYear,
      taxId,
      10000,
      Respondent("999999", "Test Bank", "1234 Test St.", "Test City", "CA", "99999"),
      parent,
      contact
    )
  }

  property("Transmittal Sheet's agency code and respondent ID match that of a given institution") {
    val institution = Institution(1, "Test Bank", Set(ExternalId("999999", RssdId), ExternalId("9876543-21", FederalTaxId)), CFPB, Bank)

    forAll(tsGen) { ts =>
      whenever(ts.id == 1) {
        S025(ts, institution) mustBe Success()
      }
    }
  }

  property("Transmittal Sheet's agency code and respondent ID does NOT match that of a given institution") {
    val institution = Institution(1, "Test Bank", Set(ExternalId("111111", RssdId), ExternalId("9876543-21", FederalTaxId)), CFPB, Bank)

    forAll(tsGen) { ts =>
      whenever(ts.id == 1) {
        S025(ts, institution) mustBe Failure()
      }
    }
  }

  property("Institution's respondent ID cannot be derived") {
    val institution = Institution(1, "Test Bank", Set(ExternalId("111111", FdicCertNo), ExternalId("9876543-21", FederalTaxId)), CFPB, Bank)

    forAll(tsGen) { ts =>
      whenever(ts.id == 1) {
        S025(ts, institution) mustBe Failure()
      }
    }
  }

}
