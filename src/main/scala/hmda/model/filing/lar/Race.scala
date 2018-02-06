package hmda.model.filing.lar

sealed trait Race {
  val code: Int
  val description: String
}

object Race {
  val values = List(1, 2, 21, 22, 23, 24, 25, 26, 27, 3, 4, 41, 42, 43, 44, 5, 6, 7)

  def valueOf(code: Int): Race = {
    code match {
      case 1 => AmericanIndianOrAlaskaNative
      case 2 => Asian
      case 21 => AsianIndian
      case 22 => Chinese
      case 23 => Filipino
      case 24 => Japanese
      case 25 => Korean
      case 26 => Vietnamese
      case 27 => OtherAsian
      case 3 => BlackOrAfricanAmerican
      case 4 => NativeHawaiianOrOtherPacificIslander
      case 41 => NativeHawaiian
      case 42 => GuamanianOrChamorro
      case 43 => Samoan
      case 44 => OtherPacificIslander
      case 5 => White
      case 6 => RaceInformationNotProvided
      case 7 => RaceNotApplicable

    }
  }
}

case object AmericanIndianOrAlaskaNative extends Race {
  override val code: Int = 1
  override val description: String = "American Indian or Alaska Native"
}

case object Asian extends Race {
  override val code: Int = 2
  override val description: String = "Asian"
}

case object AsianIndian extends Race {
  override val code: Int = 21
  override val description: String = "Asian Indian"
}

case object Chinese extends Race {
  override val code: Int = 22
  override val description: String = "Chinese"
}

case object Filipino extends Race {
  override val code: Int = 23
  override val description: String = "Filipino"
}

case object Japanese extends Race {
  override val code: Int = 24
  override val description: String = "Japanese"
}

case object Korean extends Race {
  override val code: Int = 25
  override val description: String = "Korean"
}

case object Vietnamese extends Race {
  override val code: Int = 26
  override val description: String = "Vietnamese"
}

case object OtherAsian extends Race {
  override val code: Int = 27
  override val description: String = "Other Asian"
}

case object BlackOrAfricanAmerican extends Race {
  override val code: Int = 3
  override val description: String = "Black or African American"
}

case object NativeHawaiianOrOtherPacificIslander extends Race {
  override val code: Int = 4
  override val description: String = "Native Hawaiian or Other Pacific Islander"
}

case object NativeHawaiian extends Race {
  override val code: Int = 41
  override val description: String = "Native Hawaiian"
}

case object GuamanianOrChamorro extends Race {
  override val code: Int = 42
  override val description: String = "Guamanaian or Chamorro"
}

case object Samoan extends Race {
  override val code: Int = 43
  override val description: String = "Samoan"
}

case object OtherPacificIslander extends Race {
  override val code: Int = 44
  override val description: String = "Other Pacific Islander"
}

case object White extends Race {
  override val code: Int = 5
  override val description: String = "White"
}

case object RaceInformationNotProvided extends Race {
  override val code: Int = 6
  override val description: String = "Information not provided by applicant in mail, internet, or telephone application"
}

case object RaceNotApplicable extends Race {
  override val code: Int = 7
  override val description: String = "Not applicable"
}

