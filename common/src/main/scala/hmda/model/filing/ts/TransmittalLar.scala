package hmda.model.filing.ts

import hmda.model.filing.lar.LoanApplicationRegister

import scala.concurrent.Future

case class TransmittalLar(ts: TransmittalSheet = TransmittalSheet(),
                          larsCount: Int = 0,
                          distinctcount: Int = 0)
