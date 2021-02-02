package hmda.model.filing.lar.enums

sealed trait ActionTakenTypeEnum extends LarEnum

object ActionTakenTypeEnum extends LarCodeEnum[ActionTakenTypeEnum] {
  override val values = List(1, 2, 3, 4, 5, 6, 7, 8)

  override def valueOf(code: Int): ActionTakenTypeEnum =
    code match {
      case 1 => LoanOriginated
      case 2 => ApplicationApprovedButNotAccepted
      case 3 => ApplicationDenied
      case 4 => ApplicationWithdrawnByApplicant
      case 5 => FileClosedForIncompleteness
      case 6 => PurchasedLoan
      case 7 => PreapprovalRequestDenied
      case 8 => PreapprovalRequestApprovedButNotAccepted
      case other => new InvalidActionTakenTypeCode(other)
    }
}

case object LoanOriginated extends ActionTakenTypeEnum {
  override val code: Int           = 1
  override val description: String = "Loan Originated"
}

case object ApplicationApprovedButNotAccepted extends ActionTakenTypeEnum {
  override val code: Int           = 2
  override val description: String = "Application approved but not accepted"
}

case object ApplicationDenied extends ActionTakenTypeEnum {
  override val code: Int           = 3
  override val description: String = "Application denied"
}

case object ApplicationWithdrawnByApplicant extends ActionTakenTypeEnum {
  override val code: Int           = 4
  override val description: String = "Application withdrawn by applicant"
}

case object FileClosedForIncompleteness extends ActionTakenTypeEnum {
  override val code: Int           = 5
  override val description: String = "File closed for incompleteness"
}

case object PurchasedLoan extends ActionTakenTypeEnum {
  override val code: Int           = 6
  override val description: String = "Purchased loan"
}

case object PreapprovalRequestDenied extends ActionTakenTypeEnum {
  override val code: Int           = 7
  override val description: String = "Preapproval request denied"
}

case object PreapprovalRequestApprovedButNotAccepted extends ActionTakenTypeEnum {
  override val code: Int = 8
  override val description: String =
    "Preapproval request approved but not accepted"
}

case object InvalidActionTakenTypeExemptCode extends ActionTakenTypeEnum {
  override val code: Int = 1111
  override val description: String =
    "Invalid exemption code for loan field."
}

class InvalidActionTakenTypeCode(value: Int = -1) extends ActionTakenTypeEnum {
  override def code: Int           = value
  override def description: String = "Invalid code"

  override def equals(that: Any): Boolean =
        that match {
            case that: InvalidActionTakenTypeCode => true
            case _ => false
        }
}
