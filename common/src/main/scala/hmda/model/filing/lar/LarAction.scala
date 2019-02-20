package hmda.model.filing.lar

import hmda.model.filing.lar.enums.{ActionTakenTypeEnum, InvalidActionTakenTypeCode, InvalidPreapprovalCode, PreapprovalEnum}

case class LarAction(
    preapproval: PreapprovalEnum = InvalidPreapprovalCode,
    actionTakenType: ActionTakenTypeEnum = InvalidActionTakenTypeCode,
    actionTakenDate: Int = 0
)
