package hmda.model.institution

object Parent {
  def empty: Parent = Parent(-1, None)
}

case class Parent(
  idRssd: Int,
  name: Option[String]
) {
  def isEmpty: Boolean =
    this match {
      case Parent(-1, None) => true
      case _                => false
    }
}
