package hmda.parser.fi.lar

import hmda.model.fi.lar.LoanApplicationRegister
import org.scalacheck.Gen

trait LarGenerators {

  implicit def larGen: Gen[LoanApplicationRegister] = ???


}
