package hmda.model.filing.lar

sealed trait SexObservedEnum {
  val code: Int
  val description: String
}

object SexObservedEnum {
  val values = List(1, 2, 3, 4)

  def valueOf(code: Int): SexObservedEnum = {
    code match {
      case 1 => VisualOrSurnameSex
      case 2 => NotVisualOrSurnameSex
      case 3 => SexObservedNotApplicable
      case 4 => SexObservedNoCoApplicant
      case _ => throw new Exception("Invalid Sex Observed Code")
    }
  }
}

case object VisualOrSurnameSex extends SexObservedEnum {
  override val code: Int = 1
  override val description: String =
    "Collected on the basis of visual observation or surname"
}

case object NotVisualOrSurnameSex extends SexObservedEnum {
  override val code: Int = 2
  override val description: String =
    "Not collected on the basis of visual observation or surname"
}

case object SexObservedNotApplicable extends SexObservedEnum {
  override val code: Int = 3
  override val description: String = "Not applicable"
}

case object SexObservedNoCoApplicant extends SexObservedEnum {
  override val code: Int = 4
  override val description: String = "No co-applicant"
}
