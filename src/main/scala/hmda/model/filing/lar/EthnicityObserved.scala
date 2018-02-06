package hmda.model.filing.lar

sealed trait EthnicityObserved {
  val code: Int
  val description: String
}

object EthnicityObserved {
  val values = List(1, 2, 3)

  def valueOf(code: Int): EthnicityObserved = {
    code match {
      case 1 => VisualOrSurnameEthnicity
      case 2 => NotVisualOrSurnameEthnicity
      case 3 => EthnicityObservedNotApplicable
      case _ => throw new Exception("Invalid Ethnicity Observed Code")
    }
  }
}

case object VisualOrSurnameEthnicity extends EthnicityObserved {
  override val code: Int = 1
  override val description: String =
    "Collected on the basis of visual observation or surname"
}

case object NotVisualOrSurnameEthnicity extends EthnicityObserved {
  override val code: Int = 2
  override val description: String =
    "Not collected on the basis of visual observation or surname"
}

case object EthnicityObservedNotApplicable extends EthnicityObserved {
  override val code: Int = 3
  override val description: String = "Not applicable"
}

case object EthnicityObservedNoCoApplicant extends EthnicityObserved {
  override val code: Int = 4
  override val description: String = "No co-applicant"
}
