package hmda.model.filing.lar.enums

sealed trait RaceEnum extends LarEnum

object RaceEnum extends LarCodeEnum[RaceEnum] {
  override val values =
    List(0, 1, 2, 21, 22, 23, 24, 25, 26, 27, 3, 4, 41, 42, 43, 44, 5, 6, 7, 8)

  override def valueOf(code: Int): RaceEnum =
    code match {
      case 0  => EmptyRaceValue
      case 1  => AmericanIndianOrAlaskaNative
      case 2  => Asian
      case 21 => AsianIndian
      case 22 => Chinese
      case 23 => Filipino
      case 24 => Japanese
      case 25 => Korean
      case 26 => Vietnamese
      case 27 => OtherAsian
      case 3  => BlackOrAfricanAmerican
      case 4  => NativeHawaiianOrOtherPacificIslander
      case 41 => NativeHawaiian
      case 42 => GuamanianOrChamorro
      case 43 => Samoan
      case 44 => OtherPacificIslander
      case 5  => White
      case 6  => RaceInformationNotProvided
      case 7  => RaceNotApplicable
      case 8  => RaceNoCoApplicant
      case other  => new InvalidRaceCode(other)
    }
}

case object EmptyRaceValue extends RaceEnum {
  override def code: Int           = 0
  override def description: String = "Empty Value"
}

case object AmericanIndianOrAlaskaNative extends RaceEnum {
  override val code: Int           = 1
  override val description: String = "American Indian or Alaska Native"
}

case object Asian extends RaceEnum {
  override val code: Int           = 2
  override val description: String = "Asian"
}

case object AsianIndian extends RaceEnum {
  override val code: Int           = 21
  override val description: String = "Asian Indian"
}

case object Chinese extends RaceEnum {
  override val code: Int           = 22
  override val description: String = "Chinese"
}

case object Filipino extends RaceEnum {
  override val code: Int           = 23
  override val description: String = "Filipino"
}

case object Japanese extends RaceEnum {
  override val code: Int           = 24
  override val description: String = "Japanese"
}

case object Korean extends RaceEnum {
  override val code: Int           = 25
  override val description: String = "Korean"
}

case object Vietnamese extends RaceEnum {
  override val code: Int           = 26
  override val description: String = "Vietnamese"
}

case object OtherAsian extends RaceEnum {
  override val code: Int           = 27
  override val description: String = "Other Asian"
}

case object BlackOrAfricanAmerican extends RaceEnum {
  override val code: Int           = 3
  override val description: String = "Black or African American"
}

case object NativeHawaiianOrOtherPacificIslander extends RaceEnum {
  override val code: Int           = 4
  override val description: String = "Native Hawaiian or Other Pacific Islander"
}

case object NativeHawaiian extends RaceEnum {
  override val code: Int           = 41
  override val description: String = "Native Hawaiian"
}

case object GuamanianOrChamorro extends RaceEnum {
  override val code: Int           = 42
  override val description: String = "Guamanaian or Chamorro"
}

case object Samoan extends RaceEnum {
  override val code: Int           = 43
  override val description: String = "Samoan"
}

case object OtherPacificIslander extends RaceEnum {
  override val code: Int           = 44
  override val description: String = "Other Pacific Islander"
}

case object White extends RaceEnum {
  override val code: Int           = 5
  override val description: String = "White"
}

case object RaceInformationNotProvided extends RaceEnum {
  override val code: Int = 6
  override val description: String =
    "Information not provided by applicant in mail, internet, or telephone application"
}

case object RaceNotApplicable extends RaceEnum {
  override val code: Int           = 7
  override val description: String = "Not applicable"
}

case object RaceNoCoApplicant extends RaceEnum {
  override val code: Int           = 8
  override val description: String = "No co-applicant"
}

case object InvalidRaceExemptCode extends RaceEnum {
  override val code: Int = 1111
  override val description: String =
    "Invalid exemption code for loan field."
}

class InvalidRaceCode(value: Int = -1) extends RaceEnum {
  override def code: Int           = value
  override def description: String = "Invalid Code"
  override def equals(that: Any): Boolean =
        that match {
            case that: InvalidRaceCode => true
            case _ => false
        }
}
