package hmda.model.filing.lar

sealed trait SexObserved {
  val code: Int
  val description: String
}

object SexObserved {
  val values = List(1,2,3,4)

  def valueOf(code: Int): SexObserved = {
    code match {
      case 1 => VisualOrSurnameSex
      case 2 => NotVisualOrSurnameSex
      case 3 => SexObservedNotApplicable
      case 4 => SexObservedNoCoApplicant
      case _ => throw new Exception("Invalid Sex Observed Code")
    }
  }
}

case object VisualOrSurnameSex extends SexObserved {
  override val code: Int = 1
  override val description: String = "Collected on the basis of visual observation or surname"
}

case object NotVisualOrSurnameSex extends SexObserved {
  override val code: Int = 2
  override val description: String = "Not collected on the basis of visual observation or surname"
}

case object SexObservedNotApplicable extends SexObserved {
  override val code: Int = 3
  override val description: String = "Not applicable"
}

case object SexObservedNoCoApplicant extends SexObserved {
  override val code: Int = 4
  override val description: String = "No co-applicant"
}