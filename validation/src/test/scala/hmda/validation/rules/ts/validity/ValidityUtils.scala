package hmda.validation.rules.ts.validity

import hmda.model.fi.ts.Respondent
import org.scalacheck.Gen

trait ValidityUtils {
  def respondentNotEmpty(r: Respondent): Boolean = {
    r.id != "" &&
      r.name != "" &&
      r.address != "" &&
      r.city != "" &&
      r.state != "" &&
      r.zipCode != ""
  }

  def respondentIsEmpty(r: Respondent): Boolean = {
    r.id == "" &&
      r.name == "" &&
      r.address == "" &&
      r.city == "" &&
      r.state == "" &&
      r.zipCode == ""
  }

  def badPhoneNumberGen: Gen[String] = {
    for {
      p1 <- Gen.numStr
      p2 <- Gen.numStr
      p3 <- Gen.numStr
      sep <- Gen.oneOf(List(".", "/", ""))
    } yield List(p1.take(3), p2.take(3), p3.take(4)).mkString(sep)
  }

  def invalidZipGen: Gen[String] = Gen.numStr.filter(s => !s.isEmpty && s.length != 5)

}
