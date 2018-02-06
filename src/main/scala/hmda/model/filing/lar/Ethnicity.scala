package hmda.model.filing.lar

sealed trait Ethnicity {
  val code: Int
  val description: String
}

object Ethnicity {
  val values = List(1, 11, 12, 13, 14, 2, 3, 4)

  def valueOf(code: Int): Ethnicity = {
    code match {
      case 1  => HispanicOrLatino
      case 11 => Mexican
      case 12 => PuertoRican
      case 13 => Cuban
      case 14 => OtherHispanicOrLatino
      case 2  => NotHispanicOrLatino
      case 3  => InformationNotProvided
      case 4  => EthnicityNotApplicable
      case _  => throw new Exception("Invalid Ethnicity Code")
    }
  }
}

case object HispanicOrLatino extends Ethnicity {
  override val code: Int = 1
  override val description: String = "Hispanic or Latino"
}

case object Mexican extends Ethnicity {
  override val code: Int = 11
  override val description: String = "Mexican"
}

case object PuertoRican extends Ethnicity {
  override val code: Int = 12
  override val description: String = "Puerto Rican"
}

case object Cuban extends Ethnicity {
  override val code: Int = 13
  override val description: String = "Cuban"
}

case object OtherHispanicOrLatino extends Ethnicity {
  override val code: Int = 14
  override val description: String = "Other Hispanic or Latino"
}

case object NotHispanicOrLatino extends Ethnicity {
  override val code: Int = 2
  override val description: String = "Not Hispanic or Latino"
}

case object InformationNotProvided extends Ethnicity {
  override val code: Int = 3
  override val description: String =
    "Information not provided by applicant in mail, internet, or telephone communication"
}

case object EthnicityNotApplicable extends Ethnicity {
  override val code: Int = 4
  override val description: String = "Not Applicable"
}
