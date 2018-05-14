package hmda.model.institution

sealed trait InstitutionType {
  val code: Int
  val name: String
}

object InstitutionType {
  def apply(): InstitutionType = UndeterminedInstitutionType

  def valueOf(code: Int): InstitutionType = code match {
    case -1 => UndeterminedInstitutionType
    case _  => throw new Exception("Invalid Institution Type")
  }
}

case object UndeterminedInstitutionType extends InstitutionType {
  override val code: Int = -1
  override val name: String = "Undetermined Institution Type"
}
