package hmda.model.filing.lar

import hmda.model.filing.lar.enums.{ActionTakenTypeEnum, PreapprovalEnum}

case class LarAction(
    preapproval: PreapprovalEnum,
    actionTakenType: ActionTakenTypeEnum,
    actionTakenDate: Int
)
