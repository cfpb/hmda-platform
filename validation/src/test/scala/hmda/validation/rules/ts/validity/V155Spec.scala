package hmda.validation.rules.ts.validity

import hmda.model.fi.ts.Contact
import hmda.parser.fi.ts.TsGenerators
import hmda.validation.dsl.{ Failure, Success }
import org.scalacheck.Gen
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class V155Spec extends PropSpec with PropertyChecks with MustMatchers with TsGenerators {

  property("A valid email address for the institution must be provided") {
    forAll(tsGen) { ts =>
      whenever(ts.contact.email != "") {
        V155(ts) mustBe Success()
      }
    }
  }

  property("Invalid emails should be detected") {
    forAll(tsGen) { ts =>
      val emptyContact = Contact("", "", "", "")
      val badContact = badContactGen.sample.getOrElse(emptyContact)
      val badTs = ts.copy(contact = badContact)
      val badTs2 = ts.copy(contact = badContact.copy(email = ""))
      whenever(badTs.id == 1) {
        V155(badTs) mustBe Failure("is not a valid email")
        V155(badTs2) mustBe Failure("is not a valid email")
      }
    }
  }

  implicit def badContactGen: Gen[Contact] = {
    for {
      name <- Gen.alphaStr
      phone <- phoneGen
      fax <- phoneGen
      email <- badEmailGen
    } yield Contact(name, phone, fax, email)
  }

  implicit def badEmailGen: Gen[String] = {
    for {
      name <- Gen.alphaStr.filter(s => s.nonEmpty)
      at <- Gen.oneOf("@.", "", "@@", ".@")
      domain <- Gen.alphaStr.filter(s => s.nonEmpty)
      dotCom = ".com"
    } yield List(name, at, domain, dotCom).mkString
  }

}
