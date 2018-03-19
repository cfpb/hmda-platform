package hmda.model.filing.lar.enums

sealed trait SexObservedEnum extends LarEnum

object SexObservedEnum extends LarCodeEnum[SexObservedEnum] {
  override val values = List(1, 2, 3, 4)

  override def valueOf(code: Int): SexObservedEnum = {
    code match {
      case 1 => VisualOrSurnameSex
      case 2 => NotVisualOrSurnameSex
      case 3 => SexObservedNotApplicable
      case 4 => SexObservedNoCoApplicant
      case _ => InvalidSexObservedCode
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

case object InvalidSexObservedCode extends SexObservedEnum {
  override def code: Int = -1
  override def description: String = "Invalid Code"
}
