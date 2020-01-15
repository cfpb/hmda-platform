package hmda.model.filing.lar.enums

sealed trait PayableToInstitutionEnum extends LarEnum

object PayableToInstitutionEnum extends LarCodeEnum[PayableToInstitutionEnum] {
  override val values = List(1, 2, 3, 1111)

  override def valueOf(code: Int): PayableToInstitutionEnum =
    code match {
      case 1    => InititallyPayableToInstitution
      case 2    => NotInitiallyPayableToInstitution
      case 3    => PayableToInstitutionNotApplicable
      case 1111 => PayableToInstitutionExempt
      case other   => new InvalidPayableToInstitutionCode(other)
    }
}

case object InititallyPayableToInstitution extends PayableToInstitutionEnum {
  override val code: Int           = 1
  override val description: String = "Inititally payable to your institution"
}

case object NotInitiallyPayableToInstitution extends PayableToInstitutionEnum {
  override val code: Int           = 2
  override val description: String = "Not initially payable to your institution"
}

case object PayableToInstitutionNotApplicable extends PayableToInstitutionEnum {
  override val code: Int           = 3
  override val description: String = "Not applicable"
}

case object PayableToInstitutionExempt extends PayableToInstitutionEnum {
  override def code: Int           = 1111
  override def description: String = "Exempt Payable to Institution"
}

class InvalidPayableToInstitutionCode(value: Int = -1) extends PayableToInstitutionEnum {
  override def code: Int           = value
  override def description: String = "Invalid Code"
  override def equals(that: Any): Boolean =
        that match {
            case that: InvalidPayableToInstitutionCode => true
            case _ => false
        }
}
