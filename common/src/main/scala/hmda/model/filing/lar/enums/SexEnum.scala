package hmda.model.filing.lar.enums

sealed trait SexEnum extends LarEnum

object SexEnum extends LarCodeEnum[SexEnum] {
  override val values = List(1, 2, 3, 4, 5, 6)

  override def valueOf(code: Int): SexEnum =
    code match {
      case 1 => Male
      case 2 => Female
      case 3 => SexInformationNotProvided
      case 4 => SexNotApplicable
      case 5 => SexNoCoApplicant
      case 6 => MaleAndFemale
      case other => new InvalidSexCode(other)
    }
}

case object Male extends SexEnum {
  override val code: Int           = 1
  override val description: String = "Male"
}

case object Female extends SexEnum {
  override val code: Int           = 2
  override val description: String = "Female"
}

case object SexInformationNotProvided extends SexEnum {
  override val code: Int = 3
  override val description: String =
    "Information not provided by applicant in mail, internet or telephone application"
}

case object SexNotApplicable extends SexEnum {
  override val code: Int           = 4
  override val description: String = "Not applicable"
}

case object SexNoCoApplicant extends SexEnum {
  override val code: Int           = 5
  override val description: String = "No co-applicant"
}

case object MaleAndFemale extends SexEnum {
  override val code: Int           = 6
  override val description: String = "Applicant selected both male and female"
}

case object InvalidSexExemptCode extends SexEnum {
  override val code: Int = 1111
  override val description: String =
    "Invalid exemption code for loan field."
}

class InvalidSexCode(value: Int = -1) extends SexEnum {
  override def code: Int           = value
  override def description: String = "Invalid Code"
  override def equals(that: Any): Boolean =
        that match {
            case that: InvalidSexCode => true
            case _ => false
        }
}
