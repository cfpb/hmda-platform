package hmda.validation.rules.syntactical

import hmda.model.fi.FIData
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.{ Success, Failure, Result, CommonDsl }

/*
 The first record in the file mest = 1 (TS)
 The second and all subsequent record identifiers must = 2 (LAR)
 */
object S010 extends CommonDsl {

  //Hardcoded for now
  val tsId = 1
  val larID = 2

  def apply(ts: TransmittalSheet): Result = {
    ts.id is equalTo(tsId)
  }

  def apply(lar: LoanApplicationRegister): Result = {
    lar.id is equalTo(larID)
  }

  def apply(fiData: FIData): Result = {
    val ts = fiData.ts
    val lars = fiData.lars

    val failures = lars.filter { lar =>
      (lar.id is equalTo(larID)) != Success()
    }

    val tsCheck: Result = ts.id is equalTo(tsId)
    val larCheck: Result = if (failures.nonEmpty) Failure("") else Success()

    tsCheck and larCheck
  }

}
