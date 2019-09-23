package hmda.model.institution

object TopHolder {
  def empty: TopHolder = TopHolder(-1, None)
}

case class TopHolder(
  idRssd: Int,
  name: Option[String]
) {
  def isEmpty: Boolean =
    this match {
      case TopHolder(-1, None) => true
      case _                   => false
    }
}
