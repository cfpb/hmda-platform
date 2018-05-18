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
)
