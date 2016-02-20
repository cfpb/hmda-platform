package hmda.validation.rules.syntactical

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.{ Result, CommonDsl }

/*
 Agency code must = 1,2,3,5,7,9.
 The agency that reports the data must be the same as the reported agency code
 */
object S020 extends CommonDsl {

  //Hardcoded for now
  val agencyCodes: List[Int] = List(1, 2, 3, 5, 7, 9)

  def apply(ts: TransmittalSheet): Result = {
    ts.agencyCode is containedIn(agencyCodes)
  }

  def apply(lar: LoanApplicationRegister): Result = {
    lar.agencyCode is containedIn(agencyCodes)
  }

}
