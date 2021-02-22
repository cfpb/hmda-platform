package hmda.model.filing.lar.enums

sealed trait EthnicityEnum extends LarEnum

object EthnicityEnum extends LarCodeEnum[EthnicityEnum] {
  override val values = List(0, 1, 11, 12, 13, 14, 2, 3, 4, 5)

  override def valueOf(code: Int): EthnicityEnum =
    code match {
      case 0  => EmptyEthnicityValue
      case 1  => HispanicOrLatino
      case 11 => Mexican
      case 12 => PuertoRican
      case 13 => Cuban
      case 14 => OtherHispanicOrLatino
      case 2  => NotHispanicOrLatino
      case 3  => InformationNotProvided
      case 4  => EthnicityNotApplicable
      case 5  => EthnicityNoCoApplicant
      case other  => new InvalidEthnicityCode(other)
    }
}

case object EmptyEthnicityValue extends EthnicityEnum {
  override def code: Int           = 0
  override def description: String = "Empty Value"
}

case object HispanicOrLatino extends EthnicityEnum {
  override val code: Int           = 1
  override val description: String = "Hispanic or Latino"
}

case object Mexican extends EthnicityEnum {
  override val code: Int           = 11
  override val description: String = "Mexican"
}

case object PuertoRican extends EthnicityEnum {
  override val code: Int           = 12
  override val description: String = "Puerto Rican"
}

case object Cuban extends EthnicityEnum {
  override val code: Int           = 13
  override val description: String = "Cuban"
}

case object OtherHispanicOrLatino extends EthnicityEnum {
  override val code: Int           = 14
  override val description: String = "Other Hispanic or Latino"
}

case object NotHispanicOrLatino extends EthnicityEnum {
  override val code: Int           = 2
  override val description: String = "Not Hispanic or Latino"
}

case object InformationNotProvided extends EthnicityEnum {
  override val code: Int = 3
  override val description: String =
    "Information not provided by applicant in mail, internet, or telephone communication"
}

case object EthnicityNotApplicable extends EthnicityEnum {
  override val code: Int           = 4
  override val description: String = "Not Applicable"
}

case object EthnicityNoCoApplicant extends EthnicityEnum {
  override val code: Int           = 5
  override val description: String = "No co-applicant"
}

case object InvalidEthnicityExemptCode extends EthnicityEnum {
  override val code: Int = 1111
  override val description: String =
    "Invalid exemption code for loan field."
}

class InvalidEthnicityCode(value: Int = -1) extends EthnicityEnum {
  override def code: Int           = value
  override def description: String = "Invalid Code"
  override def equals(that: Any): Boolean =
        that match {
            case that: InvalidEthnicityCode => true
            case _ => false
        }
}
