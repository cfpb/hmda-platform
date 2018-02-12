package hmda.model.filing.lar.enums

sealed trait ApplicationSubmissionEnum extends LarEnum

object ApplicationSubmissionEnum
    extends LarCodeEnum[ApplicationSubmissionEnum] {
  override val values = List(1, 2, 3)

  override def valueOf(code: Int): ApplicationSubmissionEnum = {
    code match {
      case 1 => SubmittedDirectlyToInstitution
      case 2 => NotSubmittedDirectlyToInstitution
      case 3 => ApplicationSubmissionNotApplicable
      case _ => throw new Exception("Invalid Application Submission Code")
    }
  }
}

case object SubmittedDirectlyToInstitution extends ApplicationSubmissionEnum {
  override val code: Int = 1
  override val description: String = "Submitted directly to your institution"
}

case object NotSubmittedDirectlyToInstitution
    extends ApplicationSubmissionEnum {
  override val code: Int = 2
  override val description: String =
    "Not submitted directly to your institution"
}

case object ApplicationSubmissionNotApplicable
    extends ApplicationSubmissionEnum {
  override val code: Int = 3
  override val description: String = "Not applicable"
}
