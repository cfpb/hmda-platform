package hmda.model.institution

object Respondent {
  def empty: Respondent =
    Respondent(
      None,
      None,
      None
    )
}

case class Respondent(
    name: Option[String],
    state: Option[String],
    city: Option[String]
) {
  def isEmpty: Boolean = {
    this match {
      case Respondent(None, None, None) => true
      case _                            => false
    }
  }
}
