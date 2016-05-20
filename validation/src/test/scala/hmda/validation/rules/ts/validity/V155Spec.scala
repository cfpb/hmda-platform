package hmda.validation.rules.ts.validity

import hmda.model.fi.ts.{ Contact, TransmittalSheet }
import hmda.validation.rules.ts.TsEditCheckSpec
import hmda.validation.rules.EditCheck
import org.scalacheck.Gen

class V155Spec extends TsEditCheckSpec {

  property("A valid email address for the institution must be provided") {
    forAll(tsGen) { ts =>
      whenever(ts.contact.email != "") {
        ts.mustPass
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
        badTs.mustFail
        badTs2.mustFail
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

  override def check: EditCheck[TransmittalSheet] = V155

}
