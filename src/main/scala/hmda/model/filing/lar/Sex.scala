package hmda.model.filing.lar

sealed trait Sex {
  val code: Int
  val description: String
}

object Sex {
  val values = List(1,2,3,4,5,6)


}

case object Male extends Sex {
  override val code: Int = 1
  override val description: String = "Male"
}

case object Female extends Sex {
  override val code: Int = 2
  override val description: String = "Female"
}

case object SexInformationNotProvided extends Sex {
  override val code: Int = 3
  override val description: String = "Information not provided by applicant in mail, internet or telephone application"
}

case object SexNotApplicable extends Sex {
  override val code: Int = 4
  override val description: String = "Not applicable"
}

case object SexNoCoApplicant extends Sex {
  override val code: Int = 5
  override val description: String = "No co-applicant"
}

case object MaleAndFemale extends Sex {
  override val code: Int = 6
  override val description: String = "Applicant selected both male and female"
}