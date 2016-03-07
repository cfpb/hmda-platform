package hmda.validation.rules

import hmda.model.fi.FIData
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.{ CommonDsl, Failure, Result, Success }

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

  def apply(fiData: FIData): Result = {
    val ts = fiData.ts
    val lars = fiData.lars
    val failures = lars.filter { lar =>
      (lar.agencyCode is containedIn(agencyCodes)) != Success()
    }

    val tsCheck = ts.agencyCode is containedIn(agencyCodes)
    val larCheck = if (failures.nonEmpty) Failure("Agency Code is incorrect") else Success()

    tsCheck and larCheck

  }

}
