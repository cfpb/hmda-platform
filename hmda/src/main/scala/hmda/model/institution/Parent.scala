package hmda.model.institution

object Parent {
  def empty: Parent = Parent(None, None)
}

case class Parent(
    idRssd: Option[Int],
    name: Option[String]
) {
  def isEmpty: Boolean = {
    this match {
      case Parent(None, None) => true
      case _                  => false
    }
  }
}
