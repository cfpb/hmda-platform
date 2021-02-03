package hmda.model.filing.lar.enums

sealed trait RaceObservedEnum extends LarEnum

object RaceObservedEnum extends LarCodeEnum[RaceObservedEnum] {
  override val values = List(1, 2, 3, 4)

  override def valueOf(code: Int): RaceObservedEnum =
    code match {
      case 1 => VisualOrSurnameRace
      case 2 => NotVisualOrSurnameRace
      case 3 => RaceObservedNotApplicable
      case 4 => RaceObservedNoCoApplicant
      case other => new InvalidRaceObservedCode(other)
    }
}

case object VisualOrSurnameRace extends RaceObservedEnum {
  override val code: Int = 1
  override val description: String =
    "Collected on the basis of visual observation or surname"
}

case object NotVisualOrSurnameRace extends RaceObservedEnum {
  override val code: Int = 2
  override val description: String =
    "Not collected on the basis of visual observation or surname"
}

case object RaceObservedNotApplicable extends RaceObservedEnum {
  override val code: Int           = 3
  override val description: String = "Not applicable"
}

case object RaceObservedNoCoApplicant extends RaceObservedEnum {
  override val code: Int           = 4
  override val description: String = "No co-applicant"
}

case object InvalidRaceObservedExemptCode extends RaceObservedEnum {
  override val code: Int = 1111
  override val description: String =
    "Invalid exemption code for loan field."
}

class InvalidRaceObservedCode(value: Int = -1) extends RaceObservedEnum {
  override def code: Int           = value
  override def description: String = "Invalid Code"
  override def equals(that: Any): Boolean =
        that match {
            case that: InvalidRaceObservedCode => true
            case _ => false
        }
}
