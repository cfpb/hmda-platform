package hmda.model.filing.lar.enums

sealed trait SexObservedEnum extends LarEnum

object SexObservedEnum extends LarCodeEnum[SexObservedEnum] {
  override val values = List(1, 2, 3, 4)

  override def valueOf(code: Int): SexObservedEnum =
    code match {
      case 1 => VisualOrSurnameSex
      case 2 => NotVisualOrSurnameSex
      case 3 => SexObservedNotApplicable
      case 4 => SexObservedNoCoApplicant
      case other => new InvalidSexObservedCode(other)
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
  override val code: Int           = 3
  override val description: String = "Not applicable"
}

case object SexObservedNoCoApplicant extends SexObservedEnum {
  override val code: Int           = 4
  override val description: String = "No co-applicant"
}

case object InvalidSexObservedExemptCode extends SexObservedEnum {
  override val code: Int = 1111
  override val description: String =
    "Invalid exemption code for loan field."
}

class InvalidSexObservedCode(value: Int = -1) extends SexObservedEnum {
  override def code: Int           = value
  override def description: String = "Invalid Code"
  override def equals(that: Any): Boolean =
        that match {
            case that: InvalidSexObservedCode => true
            case _ => false
        }
}
