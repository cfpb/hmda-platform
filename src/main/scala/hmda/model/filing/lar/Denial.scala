package hmda.model.filing.lar

import hmda.model.filing.lar.enums.{DenialReasonEnum, InvalidDenialReasonCode}

case class Denial(
    denialReason1: DenialReasonEnum = InvalidDenialReasonCode,
    denialReason2: DenialReasonEnum = InvalidDenialReasonCode,
    denialReason3: DenialReasonEnum = InvalidDenialReasonCode,
    denialReason4: DenialReasonEnum = InvalidDenialReasonCode,
    otherDenialReason: String = ""
)
