package hmda.model.fi

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.ts.TransmittalSheet
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.JSConverters._

case class FIData(ts: TransmittalSheet, lars: Iterable[LoanApplicationRegister]) {
  def toCSV: String = {
    s"${ts.toCSV}\n" +
      s"${lars.map(lar => lar.toCSV + "\n")}"
  }

  @JSExport("ts")
  def tsJS: TransmittalSheet = ts

  @JSExport("lars")
  def larsJS: scala.scalajs.js.Array[LoanApplicationRegister] = lars.toJSArray

}
