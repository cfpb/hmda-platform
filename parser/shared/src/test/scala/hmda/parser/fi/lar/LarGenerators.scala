package hmda.parser.fi.lar

import hmda.model.fi.lar._
import hmda.parser.fi.FIGenerators
import org.scalacheck.Gen

trait LarGenerators extends FIGenerators {

  //TODO: implement LarGenerators

  implicit def larGen: Gen[LoanApplicationRegister] = ???

  implicit def loanGen: Gen[Loan] = ???

  implicit def preapprovalGen: Gen[Int] = ???

  implicit def actionTypeGen: Gen[Int] = ???

  implicit def actionDateGen: Gen[Int] = ???

  implicit def geographyGen: Gen[Geography] = ???

  implicit def applicantGen: Gen[Applicant] = ???

  implicit def purchaserTypeGen: Gen[Int] = ???

  implicit def denialGen: Gen[Denial] = ???

  implicit def rateSpreadGen: Gen[String] = ???

  implicit def hoepaStatusGen: Gen[Int] = ???

  implicit def lienStatusGen: Gen[Int] = ???

}
