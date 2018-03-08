package hmda.model.filing.lar.enums

sealed trait EthnicityObservedEnum extends LarEnum

object EthnicityObservedEnum extends LarCodeEnum[EthnicityObservedEnum] {
  override val values = List(1, 2, 3)

  override def valueOf(code: Int): EthnicityObservedEnum = {
    code match {
      case 1 => VisualOrSurnameEthnicity
      case 2 => NotVisualOrSurnameEthnicity
      case 3 => EthnicityObservedNotApplicable
      case _ => throw new Exception("Invalid Ethnicity Observed Code")
    }
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
  override val code: Int = 3
  override val description: String = "Not applicable"
}

case object EthnicityObservedNoCoApplicant extends EthnicityObservedEnum {
  override val code: Int = 4
  override val description: String = "No co-applicant"
}
