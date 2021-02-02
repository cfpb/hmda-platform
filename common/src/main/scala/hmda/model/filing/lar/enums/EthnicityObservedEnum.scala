package hmda.model.filing.lar.enums

sealed trait EthnicityObservedEnum extends LarEnum

object EthnicityObservedEnum extends LarCodeEnum[EthnicityObservedEnum] {
  override val values = List(1, 2, 3, 4)

  override def valueOf(code: Int): EthnicityObservedEnum =
    code match {
      case 1 => VisualOrSurnameEthnicity
      case 2 => NotVisualOrSurnameEthnicity
      case 3 => EthnicityObservedNotApplicable
      case 4 => EthnicityObservedNoCoApplicant
      case other => new InvalidEthnicityObservedCode(other)
    }
}

case object VisualOrSurnameEthnicity extends EthnicityObservedEnum {
  override val code: Int = 1
  override val description: String =
    "Collected on the basis of visual observation or surname"
}

case object NotVisualOrSurnameEthnicity extends EthnicityObservedEnum {
  override val code: Int = 2
  override val description: String =
    "Not collected on the basis of visual observation or surname"
}

case object EthnicityObservedNotApplicable extends EthnicityObservedEnum {
  override val code: Int           = 3
  override val description: String = "Not applicable"
}

case object EthnicityObservedNoCoApplicant extends EthnicityObservedEnum {
  override val code: Int           = 4
  override val description: String = "No co-applicant"
}

case object InvalidEthnicityObservedExemptCode extends EthnicityObservedEnum {
  override val code: Int = 1111
  override val description: String =
    "Invalid exemption code for loan field."
}

class InvalidEthnicityObservedCode(value: Int = -1) extends EthnicityObservedEnum {
  override def code: Int           = value
  override def description: String = "Invalid Code"
  override def equals(that: Any): Boolean =
        that match {
            case that: InvalidEthnicityObservedCode => true
            case _ => false
        }
}
