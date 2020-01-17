package hmda.model.filing.lar.enums

sealed trait AutomatedUnderwritingResultEnum extends LarEnum

object AutomatedUnderwritingResultEnum
    extends LarCodeEnum[AutomatedUnderwritingResultEnum] {
  override val values = (0 to 24).toList :+ 1111

  override def valueOf(code: Int): AutomatedUnderwritingResultEnum =
    code match {
      case 0    => EmptyAUSResultValue
      case 1    => ApproveEligible
      case 2    => ApproveIneligible
      case 3    => ReferEligilbe
      case 4    => ReferIneligible
      case 5    => ReferWithCaution
      case 6    => OutOfScope
      case 7    => Error
      case 8    => Accept
      case 9    => Caution
      case 10   => Ineligible
      case 11   => Incomplete
      case 12   => Invalid
      case 13   => Refer
      case 14   => Eligible
      case 15   => UnableToDetermineOrUnknown
      case 16   => OtherAutomatedUnderwritingResult
      case 17   => AutomatedUnderwritingResultNotApplicable
      case 18   => AcceptEligible
      case 19   => AcceptIneligible
      case 20   => AcceptUnableToDetermine
      case 21   => ReferWithCautionEligible
      case 22   => ReferWithCautionIneligible
      case 23   => ReferUnableToDetermine
      case 24   => ReferWithCautionUnableToDetermine
      case 1111 => AUSResultExempt
      case other    => new InvalidAutomatedUnderwritingResultCode(other)
    }
}

case object EmptyAUSResultValue extends AutomatedUnderwritingResultEnum {
  override def code: Int           = 0
  override def description: String = "Empty Value"
}

case object ApproveEligible extends AutomatedUnderwritingResultEnum {
  override val code: Int           = 1
  override val description: String = "Approve/Eligible"
}

case object ApproveIneligible extends AutomatedUnderwritingResultEnum {
  override val code: Int           = 2
  override val description: String = "Approve/Ineligible"
}

case object ReferEligilbe extends AutomatedUnderwritingResultEnum {
  override val code: Int           = 3
  override val description: String = "Refer/Eligible"
}

case object ReferIneligible extends AutomatedUnderwritingResultEnum {
  override val code: Int           = 4
  override val description: String = "Refer/Ineligible"
}

case object ReferWithCaution extends AutomatedUnderwritingResultEnum {
  override val code: Int           = 5
  override val description: String = "Refer with Caution"
}

case object OutOfScope extends AutomatedUnderwritingResultEnum {
  override val code: Int           = 6
  override val description: String = "Out of Scope"
}

case object Error extends AutomatedUnderwritingResultEnum {
  override val code: Int           = 7
  override val description: String = "Error"
}

case object Accept extends AutomatedUnderwritingResultEnum {
  override val code: Int           = 8
  override val description: String = "Accept"
}

case object Caution extends AutomatedUnderwritingResultEnum {
  override val code: Int           = 9
  override val description: String = "Caution"
}

case object Ineligible extends AutomatedUnderwritingResultEnum {
  override val code: Int           = 10
  override val description: String = "Ineligible"
}

case object Incomplete extends AutomatedUnderwritingResultEnum {
  override val code: Int           = 11
  override val description: String = "Incomplete"
}

case object Invalid extends AutomatedUnderwritingResultEnum {
  override val code: Int           = 12
  override val description: String = "Invalid"
}

case object Refer extends AutomatedUnderwritingResultEnum {
  override val code: Int           = 13
  override val description: String = "Refer"
}

case object Eligible extends AutomatedUnderwritingResultEnum {
  override val code: Int           = 14
  override val description: String = "Eligible"
}

case object UnableToDetermineOrUnknown extends AutomatedUnderwritingResultEnum {
  override val code: Int           = 15
  override val description: String = "Unable to Determine or Unknown"
}

case object OtherAutomatedUnderwritingResult extends AutomatedUnderwritingResultEnum {
  override val code: Int           = 16
  override val description: String = "Other"
}

case object AutomatedUnderwritingResultNotApplicable extends AutomatedUnderwritingResultEnum {
  override val code: Int           = 17
  override val description: String = "Not applicable"
}

case object AcceptEligible extends AutomatedUnderwritingResultEnum {
  override def code: Int           = 18
  override def description: String = "Accept/Eligible"
}

case object AcceptIneligible extends AutomatedUnderwritingResultEnum {
  override def code: Int           = 19
  override def description: String = "Accept/Ineligible"
}

case object AcceptUnableToDetermine extends AutomatedUnderwritingResultEnum {
  override def code: Int           = 20
  override def description: String = "Accept/Unable to Determine"
}

case object ReferWithCautionEligible extends AutomatedUnderwritingResultEnum {
  override def code: Int           = 21
  override def description: String = "Refer with Caution/Eligible"
}

case object ReferWithCautionIneligible extends AutomatedUnderwritingResultEnum {
  override def code: Int           = 22
  override def description: String = "Refer with Caution/Ineligible"
}

case object ReferUnableToDetermine extends AutomatedUnderwritingResultEnum {
  override def code: Int           = 23
  override def description: String = "Refer/Unable to Determine"
}

case object ReferWithCautionUnableToDetermine extends AutomatedUnderwritingResultEnum {
  override def code: Int           = 24
  override def description: String = "Refer with Caution/Unable to Determine"
}

case object AUSResultExempt extends AutomatedUnderwritingResultEnum {
  override def code: Int           = 1111
  override def description: String = "Exempt AUSResult"
}

class InvalidAutomatedUnderwritingResultCode(value: Int = -1) extends AutomatedUnderwritingResultEnum {
  override def code: Int           = value
  override def description: String = "Invalid Code"
  override def equals(that: Any): Boolean =
        that match {
            case that: InvalidAutomatedUnderwritingResultCode => true
            case _ => false
        }
}
