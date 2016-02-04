package hmda.model.fi

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.ts.TransmittalSheet

case class FIData(ts: TransmittalSheet, lars: Iterator[LoanApplicationRegister]) {
  def toCSV: String = {
    s"${ts.toCSV}\n" +
      s"${lars.map(lar => lar.toCSV + "\n")}"
  }
}
