package hmda.model.fi

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.ts.TransmittalSheet

case class FIData(ts: TransmittalSheet, lars: Iterable[LoanApplicationRegister]) {
  def toCSV: String = {
    s"${ts.toCSV}\n" +
      lars.map(lar => lar.toCSV).mkString("", "\n", "\n")
  }

}
