package hmda.model.institution

object TopHolder {
  def empty: TopHolder = TopHolder(None, None)
}

case class TopHolder(
    idRssd: Option[Int],
    name: Option[String]
) {
  def isEmpty: Boolean = {
    this match {
      case TopHolder(None, None) => true
      case _                     => false
    }
  }
}
