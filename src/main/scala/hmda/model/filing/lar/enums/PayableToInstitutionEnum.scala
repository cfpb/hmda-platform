package hmda.model.filing.lar.enums

sealed trait PayableToInstitutionEnum extends LarEnum

object PayableToInstitutionEnum extends LarCodeEnum[PayableToInstitutionEnum] {
  override val values = List(1, 2, 3)

  override def valueOf(code: Int): PayableToInstitutionEnum = {
    code match {
      case 1 => NotInitiallyPayableToInstitution
      case 2 => NotInitiallyPayableToInstitution
      case 3 => PayableToInstitutionNotApplicable
      case _ => InvalidPayableToInstitutionCode
    }
  }
}

case object InititallyPayableToInstitution extends PayableToInstitutionEnum {
  override val code: Int = 1
  override val description: String = "Inititally payable to your institution"
}

case object NotInitiallyPayableToInstitution extends PayableToInstitutionEnum {
  override val code: Int = 2
  override val description: String = "Not initially payable to your institution"
}

case object PayableToInstitutionNotApplicable extends PayableToInstitutionEnum {
  override val code: Int = 3
  override val description: String = "Not applicable"
}

case object InvalidPayableToInstitutionCode extends PayableToInstitutionEnum {
  override def code: Int = -1
  override def description: String = "Invalid Code"
}
