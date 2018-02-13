package hmda.model.filing.lar

import hmda.model.filing.PipeDelimited
import hmda.model.filing.lar.enums.DenialReasonEnum

case class Denial(
    denialReason1: DenialReasonEnum,
    denialReason2: DenialReasonEnum,
    denialReason3: DenialReasonEnum,
    denialReason4: DenialReasonEnum
) extends PipeDelimited {
  override def toCSV: String = {
    s"${denialReason1.code}|${denialReason2.code}|${denialReason3.code}|${denialReason4.code}"
  }
}
